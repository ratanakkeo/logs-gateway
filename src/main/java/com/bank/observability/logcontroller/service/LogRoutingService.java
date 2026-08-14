package com.bank.observability.logcontroller.service;

import com.bank.observability.logcontroller.config.KafkaTopicProperties;
import com.bank.observability.logcontroller.model.LogPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogRoutingService {

    private static final Logger log = LoggerFactory.getLogger(LogRoutingService.class);

    private final KafkaTemplate<String, LogPayload> kafkaTemplate;
    private final KafkaTopicProperties topics;

    public LogRoutingService(KafkaTemplate<String, LogPayload> kafkaTemplate, KafkaTopicProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void route(LogPayload payload) {
        String topic = "AUDIT".equalsIgnoreCase(payload.getLogType())
                ? topics.getAudit()
                : topics.getApp();
        kafkaTemplate.send(topic, payload.getTraceId(), payload);
        log.info("log_routed topic={} traceId={} logType={}", topic, payload.getTraceId(), payload.getLogType());
    }
}
