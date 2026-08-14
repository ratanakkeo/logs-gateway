package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaLogPublisherTest {

    @Mock
    private KafkaTemplate<String, LogPayload> kafkaTemplate;

    @InjectMocks
    private KafkaLogPublisher publisher;

    @Test
    void publishesWithTraceIdPartitionKey() {
        LogPayload payload = LogPayload.builder()
                .traceId("trace-1")
                .logType("APPLICATION")
                .build();

        publisher.publish("bank.logs.app", "trace-1", payload);

        verify(kafkaTemplate).send("bank.logs.app", "trace-1", payload);
    }
}
