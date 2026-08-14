package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import com.bank.observability.logsgateway.ingest.domain.LogPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogPublisher implements LogPublisher {

    private final KafkaTemplate<String, LogEnvelope> kafkaTemplate;

    public KafkaLogPublisher(KafkaTemplate<String, LogEnvelope> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String partitionKey, LogEnvelope payload) {
        kafkaTemplate.send(topic, partitionKey, payload);
    }
}
