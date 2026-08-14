package com.bank.observability.logcontroller.service;

import com.bank.observability.logcontroller.config.KafkaTopicProperties;
import com.bank.observability.logcontroller.model.LogPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogRoutingService {

    private final KafkaTemplate<String, LogPayload> kafkaTemplate;
    private final KafkaTopicProperties topics;

    public void route(LogPayload payload) {
        String topic = "AUDIT".equalsIgnoreCase(payload.getLogType())
                ? topics.getAudit()
                : topics.getApp();
        kafkaTemplate.send(topic, payload.getTraceId(), payload);
    }
}
