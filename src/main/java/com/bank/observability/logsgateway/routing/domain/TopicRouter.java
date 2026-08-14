package com.bank.observability.logsgateway.routing.domain;

import com.bank.observability.logsgateway.config.KafkaTopicProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TopicRouter {

    static final String DEFAULT_APP_TOPIC = "bank.logs.app";

    private final KafkaTopicProperties topics;

    public TopicRouter(KafkaTopicProperties topics) {
        this.topics = topics;
    }

    public String resolve(LogEnvelope payload) {
        if (payload.logType() != null && "AUDIT".equalsIgnoreCase(payload.logType())) {
            return topics.getAudit();
        }
        return StringUtils.hasText(topics.getApp()) ? topics.getApp() : DEFAULT_APP_TOPIC;
    }
}
