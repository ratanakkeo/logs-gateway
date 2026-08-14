package com.bank.observability.logsgateway.ingest.domain;

import java.time.Instant;
import java.util.Map;

public record LogEnvelope(
        Instant timestamp,
        String serviceName,
        String traceId,
        String logLevel,
        String logType,
        String message,
        Map<String, Object> data
) {
    public LogEnvelope withTimestamp(Instant newTimestamp) {
        return new LogEnvelope(newTimestamp, serviceName, traceId, logLevel, logType, message, data);
    }

    public LogEnvelope withMessage(String newMessage) {
        return new LogEnvelope(timestamp, serviceName, traceId, logLevel, logType, newMessage, data);
    }

    public LogEnvelope withData(Map<String, Object> newData) {
        return new LogEnvelope(timestamp, serviceName, traceId, logLevel, logType, message, newData);
    }
}
