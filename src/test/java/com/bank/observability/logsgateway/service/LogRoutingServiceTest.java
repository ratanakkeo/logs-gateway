package com.bank.observability.logsgateway.service;

import com.bank.observability.logsgateway.config.KafkaTopicProperties;
import com.bank.observability.logsgateway.model.LogPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogRoutingServiceTest {

    @Mock
    private KafkaTemplate<String, LogPayload> kafkaTemplate;

    private LogRoutingService service;

    @BeforeEach
    void setUp() {
        KafkaTopicProperties topics = new KafkaTopicProperties();
        topics.setApp("bank.logs.app");
        topics.setAudit("bank.logs.audit");
        service = new LogRoutingService(kafkaTemplate, topics);
    }

    @Test
    void routesAuditLogsToAuditTopicUsingTraceIdKey() {
        LogPayload payload = LogPayload.builder()
                .traceId("trace-audit")
                .logType("AUDIT")
                .message("login success")
                .build();

        service.route(payload);

        verify(kafkaTemplate).send("bank.logs.audit", "trace-audit", payload);
    }

    @Test
    void routesApplicationLogsToAppTopicUsingTraceIdKey() {
        LogPayload payload = LogPayload.builder()
                .traceId("trace-app")
                .logType("INFO")
                .logLevel("ERROR")
                .message("timeout")
                .build();

        service.route(payload);

        verify(kafkaTemplate).send("bank.logs.app", "trace-app", payload);
    }
}
