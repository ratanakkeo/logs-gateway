package com.bank.observability.logsgateway.ingest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LogPayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void jacksonRoundTripPreservesFields() throws Exception {
        LogPayload original = LogPayload.builder()
                .timestamp(Instant.parse("2026-08-10T07:00:00Z"))
                .serviceName("payments-api")
                .traceId("trace-123")
                .logLevel("INFO")
                .logType("APPLICATION")
                .message("payment authorized")
                .data(Map.of("amount", "10.00"))
                .build();

        String json = objectMapper.writeValueAsString(original);
        LogPayload restored = objectMapper.readValue(json, LogPayload.class);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
        assertThat(json).contains("payments-api", "trace-123", "APPLICATION");
    }
}
