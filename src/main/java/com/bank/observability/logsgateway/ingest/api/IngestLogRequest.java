package com.bank.observability.logsgateway.ingest.api;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IngestLogRequest(
        Instant timestamp,
        @NotBlank String serviceName,
        @NotBlank String traceId,
        String logLevel,
        @NotBlank String logType,
        String message,
        Map<String, Object> data
) {
    public LogEnvelope toEnvelope() {
        return new LogEnvelope(timestamp, serviceName, traceId, logLevel, logType, message, data);
    }
}
