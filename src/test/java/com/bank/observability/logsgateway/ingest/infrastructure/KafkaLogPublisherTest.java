package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaLogPublisherTest {

    @Mock
    private KafkaTemplate<String, LogEnvelope> kafkaTemplate;

    @InjectMocks
    private KafkaLogPublisher publisher;

    @Test
    void publishesWithTraceIdPartitionKey() {
        LogEnvelope payload = new LogEnvelope(null, null, "trace-1", null, "APPLICATION", null, null);

        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish("bank.logs.app", "trace-1", payload);

        verify(kafkaTemplate).send("bank.logs.app", "trace-1", payload);
    }
}
