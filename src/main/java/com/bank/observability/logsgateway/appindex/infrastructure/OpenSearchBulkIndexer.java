package com.bank.observability.logsgateway.appindex.infrastructure;

import com.bank.observability.logsgateway.appindex.domain.LogIndexWriter;
import com.bank.observability.logsgateway.config.DownstreamGuard;
import com.bank.observability.logsgateway.config.OpenSearchProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenSearchBulkIndexer implements LogIndexWriter {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchBulkIndexer.class);

    private final OpenSearchClient openSearchClient;
    private final OpenSearchProperties properties;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final List<LogEnvelope> buffer = new ArrayList<>();

    public OpenSearchBulkIndexer(
            OpenSearchClient openSearchClient,
            OpenSearchProperties properties,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this(openSearchClient, properties, retryRegistry.retry("opensearch"),
                circuitBreakerRegistry.circuitBreaker("opensearch"));
    }

    OpenSearchBulkIndexer(
            OpenSearchClient openSearchClient,
            OpenSearchProperties properties,
            Retry retry,
            CircuitBreaker circuitBreaker) {
        this.openSearchClient = openSearchClient;
        this.properties = properties;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public synchronized void index(LogEnvelope payload) {
        buffer.add(payload);
        if (buffer.size() >= properties.getBulkSize()) {
            flush();
        }
    }

    @Scheduled(fixedDelayString = "${app.opensearch.flush-interval-ms:5000}")
    public synchronized void scheduledFlush() {
        flush();
    }

    synchronized void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        List<LogEnvelope> snapshot = List.copyOf(buffer);
        buffer.clear();
        bulkIndex(snapshot);
    }

    private void bulkIndex(List<LogEnvelope> batch) {
        DownstreamGuard.run(retry, circuitBreaker, () -> {
            BulkRequest.Builder builder = new BulkRequest.Builder();
            for (LogEnvelope envelope : batch) {
                builder.operations(op -> op.index(idx -> idx
                        .index(properties.getIndex())
                        .document(envelope)));
            }
            BulkResponse response = openSearchClient.bulk(builder.build());
            if (response.errors()) {
                throw new IllegalStateException("OpenSearch bulk contained item errors");
            }
            log.debug("opensearch_bulk_ok size={}", batch.size());
        });
    }
}
