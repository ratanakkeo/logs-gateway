package com.bank.observability.logsgateway.appindex.infrastructure;

import com.bank.observability.logsgateway.appindex.domain.LogIndexWriter;
import com.bank.observability.logsgateway.config.OpenSearchProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenSearchBulkIndexer implements LogIndexWriter {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchBulkIndexer.class);
    private static final int MAX_ATTEMPTS = 4;

    private final OpenSearchClient openSearchClient;
    private final OpenSearchProperties properties;
    private final List<LogEnvelope> buffer = new ArrayList<>();

    public OpenSearchBulkIndexer(OpenSearchClient openSearchClient, OpenSearchProperties properties) {
        this.openSearchClient = openSearchClient;
        this.properties = properties;
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
        bulkWithBackoff(snapshot);
    }

    private void bulkWithBackoff(List<LogEnvelope> batch) {
        IOException last = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
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
                return;
            } catch (IOException ex) {
                last = ex;
                sleep(attempt);
            }
        }
        throw new IllegalStateException("OpenSearch bulk indexing failed", last);
    }

    private static void sleep(int attempt) {
        try {
            Thread.sleep((long) Math.pow(2, attempt) * 100L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenSearch bulk retry interrupted", ex);
        }
    }
}
