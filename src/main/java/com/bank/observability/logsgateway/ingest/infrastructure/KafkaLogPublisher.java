package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import com.bank.observability.logsgateway.ingest.domain.LogPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogPublisher implements LogPublisher {

    private final KafkaTemplate<String, LogPayload> kafkaTemplate;

    public KafkaLogPublisher(KafkaTemplate<String, LogPayload> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String partitionKey, LogPayload payload) {
        kafkaTemplate.send(topic, partitionKey, payload);
    }
}
