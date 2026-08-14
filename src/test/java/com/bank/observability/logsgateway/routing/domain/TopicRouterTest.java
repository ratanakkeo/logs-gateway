package com.bank.observability.logsgateway.routing.domain;

import com.bank.observability.logsgateway.config.KafkaTopicProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
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
        LogEnvelope payload = new LogEnvelope(null, null, "trace-audit", null, "AUDIT", "login success", null);

        assertThat(router.resolve(payload)).isEqualTo("bank.logs.audit");
    }

    @Test
    void routesApplicationLogsToAppTopicUsingTraceIdKey() {
        LogEnvelope payload = new LogEnvelope(null, null, "trace-app", "ERROR", "INFO", "timeout", null);

        assertThat(router.resolve(payload)).isEqualTo("bank.logs.app");
    }

    @Test
    void defaultsMissingAppTopicToBankLogsApp() {
        KafkaTopicProperties topics = new KafkaTopicProperties();
        topics.setAudit("bank.logs.audit");
        TopicRouter defaultingRouter = new TopicRouter(topics);

        LogEnvelope payload = new LogEnvelope(null, null, null, null, "APPLICATION", null, null);

        assertThat(defaultingRouter.resolve(payload)).isEqualTo("bank.logs.app");
    }
}
