package com.bank.observability.logcontroller.model;

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
public class LogPayload {
    private Instant timestamp;
    private String serviceName;
    private String traceId;
    private String logLevel;
    private String logType;
    private String message;
    private Map<String, Object> data;
}
