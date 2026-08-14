package com.bank.observability.logsgateway.ingest.domain;

public interface LogPublisher {

    void publish(String topic, String partitionKey, LogEnvelope payload);
}
