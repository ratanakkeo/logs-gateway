package com.bank.observability.logsgateway.ingest.infrastructure;

import com.bank.observability.logsgateway.config.DownstreamGuard;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import com.bank.observability.logsgateway.ingest.domain.LogPublisher;
import com.bank.observability.logsgateway.shared.metrics.LogsGatewayMetrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogPublisher implements LogPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaLogPublisher.class);

    private final KafkaTemplate<String, LogEnvelope> kafkaTemplate;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final LogsGatewayMetrics metrics;

    public KafkaLogPublisher(
            KafkaTemplate<String, LogEnvelope> kafkaTemplate,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry,
            LogsGatewayMetrics metrics) {
        this(kafkaTemplate, retryRegistry.retry("kafka"), circuitBreakerRegistry.circuitBreaker("kafka"), metrics);
    }

    KafkaLogPublisher(
            KafkaTemplate<String, LogEnvelope> kafkaTemplate,
            Retry retry,
            CircuitBreaker circuitBreaker,
            LogsGatewayMetrics metrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
    }

    @Override
    public void publish(String topic, String partitionKey, LogEnvelope payload) {
        DownstreamGuard.run(retry, circuitBreaker, () ->
                kafkaTemplate.send(topic, partitionKey, payload).whenComplete((result, error) -> {
                    if (error != null) {
                        metrics.recordKafkaSendFailure();
                        log.error("kafka_send_failed topic={} key={} serviceName={} logType={}",
                                topic, partitionKey, payload.serviceName(), payload.logType(), error);
                    }
                }));
    }
}
