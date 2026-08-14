package com.bank.observability.logsgateway.routing.domain;

import com.bank.observability.logsgateway.config.KafkaTopicProperties;
import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicRouterTest {

    private TopicRouter router;

    @BeforeEach
    void setUp() {
        KafkaTopicProperties topics = new KafkaTopicProperties();
        topics.setApp("bank.logs.app");
        topics.setAudit("bank.logs.audit");
        router = new TopicRouter(topics);
    }

    @Test
    void routesAuditLogsToAuditTopicUsingTraceIdKey() {
        LogPayload payload = LogPayload.builder()
                .traceId("trace-audit")
                .logType("AUDIT")
                .message("login success")
                .build();

        assertThat(router.resolve(payload)).isEqualTo("bank.logs.audit");
    }

    @Test
    void routesApplicationLogsToAppTopicUsingTraceIdKey() {
        LogPayload payload = LogPayload.builder()
                .traceId("trace-app")
                .logType("INFO")
                .logLevel("ERROR")
                .message("timeout")
                .build();

        assertThat(router.resolve(payload)).isEqualTo("bank.logs.app");
    }

    @Test
    void defaultsMissingAppTopicToBankLogsApp() {
        KafkaTopicProperties topics = new KafkaTopicProperties();
        topics.setAudit("bank.logs.audit");
        TopicRouter defaultingRouter = new TopicRouter(topics);

        LogPayload payload = LogPayload.builder().logType("APPLICATION").build();

        assertThat(defaultingRouter.resolve(payload)).isEqualTo("bank.logs.app");
    }
}
