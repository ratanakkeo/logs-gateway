package com.bank.observability.logsgateway.ingest.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogPayload {
    private Instant timestamp;
    @NotBlank
    private String serviceName;
    @NotBlank
    private String traceId;
    private String logLevel;
    @NotBlank
    private String logType;
    private String message;
    private Map<String, Object> data;
}
