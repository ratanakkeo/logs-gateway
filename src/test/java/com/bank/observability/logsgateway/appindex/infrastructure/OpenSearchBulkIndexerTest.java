package com.bank.observability.logsgateway.appindex.infrastructure;

import com.bank.observability.logsgateway.config.OpenSearchProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenSearchBulkIndexerTest {

    @Mock
    private OpenSearchClient openSearchClient;

    private OpenSearchBulkIndexer indexer;

    @BeforeEach
    void setUp() {
        OpenSearchProperties properties = new OpenSearchProperties();
        properties.setIndex("bank-logs-app");
        properties.setBulkSize(1);
        Retry retry = Retry.of("opensearch", RetryConfig.custom()
                .maxAttempts(4)
                .waitDuration(Duration.ZERO)
                .retryExceptions(IOException.class)
                .build());
        indexer = new OpenSearchBulkIndexer(
                openSearchClient, properties, retry, CircuitBreaker.ofDefaults("opensearch"));
    }

    @Test
    void retriesIoFailuresThenSucceeds() throws Exception {
        BulkResponse ok = mock(BulkResponse.class);
        when(ok.errors()).thenReturn(false);
        when(openSearchClient.bulk(any(BulkRequest.class)))
                .thenThrow(new IOException("timeout"))
                .thenThrow(new IOException("timeout"))
                .thenReturn(ok);

        indexer.index(envelope());

        verify(openSearchClient, times(3)).bulk(any(BulkRequest.class));
    }

    @Test
    void poisonBulkErrorsAreNotRetried() throws Exception {
        BulkResponse failed = mock(BulkResponse.class);
        when(failed.errors()).thenReturn(true);
        when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(failed);

        assertThatThrownBy(() -> indexer.index(envelope()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("item errors");

        verify(openSearchClient, times(1)).bulk(any(BulkRequest.class));
    }

    private static LogEnvelope envelope() {
        return new LogEnvelope(null, "auth-api", "trace-os", null, "APPLICATION", "ok", null);
    }
}
