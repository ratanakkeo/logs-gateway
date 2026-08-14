package com.bank.observability.logsgateway.appindex.infrastructure;

import com.bank.observability.logsgateway.appindex.domain.LogIndexWriter;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@Tag("it")
@ActiveProfiles("test")
class KafkaAppLogAckIT {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @MockitoBean
    private LogIndexWriter logIndexWriter;

    @Autowired
    private KafkaTemplate<String, LogEnvelope> kafkaTemplate;

    @Test
    void appListenerIndexesAndAcknowledges() {
        LogEnvelope envelope = new LogEnvelope(
                Instant.parse("2026-08-10T07:00:00Z"),
                "payments-api",
                "trace-it",
                "INFO",
                "APPLICATION",
                "it ack path",
                null);

        kafkaTemplate.send("bank.logs.app", envelope.traceId(), envelope);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                verify(logIndexWriter).index(argThat(payload -> "trace-it".equals(payload.traceId()))));
    }
}
