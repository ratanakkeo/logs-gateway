package com.bank.observability.logsgateway.ingest.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;

public interface LogPublisher {

    void publish(String topic, String partitionKey, LogPayload payload);
}
