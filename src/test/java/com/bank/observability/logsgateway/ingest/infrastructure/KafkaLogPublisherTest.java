package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaLogPublisherTest {

    @Mock
    private KafkaTemplate<String, LogEnvelope> kafkaTemplate;

    private KafkaLogPublisher publisher;

    @BeforeEach
    void setUp() {
        Retry retry = Retry.of("kafka", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ZERO)
                .build());
        publisher = new KafkaLogPublisher(kafkaTemplate, retry, CircuitBreaker.ofDefaults("kafka"));
    }

    @Test
    void publishesWithTraceIdPartitionKey() {
        LogEnvelope payload = new LogEnvelope(null, null, "trace-1", null, "APPLICATION", null, null);

        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish("bank.logs.app", "trace-1", payload);

        verify(kafkaTemplate).send("bank.logs.app", "trace-1", payload);
    }

    @Test
    void retriesWhenSendThrows() {
        LogEnvelope payload = new LogEnvelope(null, "payments-api", "trace-1", null, "APPLICATION", null, null);
        when(kafkaTemplate.send(any(), any(), any())).thenThrow(new IllegalStateException("broker unavailable"));

        assertThatThrownBy(() -> publisher.publish("bank.logs.app", "trace-1", payload))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("broker unavailable");

        verify(kafkaTemplate, times(3)).send("bank.logs.app", "trace-1", payload);
    }
}
