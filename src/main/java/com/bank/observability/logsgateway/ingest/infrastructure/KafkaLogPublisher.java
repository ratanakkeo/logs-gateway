package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import com.bank.observability.logsgateway.ingest.domain.LogPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogPublisher implements LogPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaLogPublisher.class);

    private final KafkaTemplate<String, LogEnvelope> kafkaTemplate;

    public KafkaLogPublisher(KafkaTemplate<String, LogEnvelope> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String partitionKey, LogEnvelope payload) {
        kafkaTemplate.send(topic, partitionKey, payload).whenComplete((result, error) -> {
            if (error != null) {
                log.error("kafka_send_failed topic={} key={} serviceName={} logType={}",
                        topic, partitionKey, payload.serviceName(), payload.logType(), error);
            }
        });
    }
}
