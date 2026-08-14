package com.bank.observability.logsgateway.ingest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IngestLogRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void jacksonRoundTripPreservesFields() throws Exception {
        IngestLogRequest original = new IngestLogRequest(
                Instant.parse("2026-08-10T07:00:00Z"),
                "payments-api",
                "trace-123",
                "INFO",
                "APPLICATION",
                "payment authorized",
                Map.of("amount", "10.00"));

        String json = objectMapper.writeValueAsString(original);
        IngestLogRequest restored = objectMapper.readValue(json, IngestLogRequest.class);

        assertThat(restored).isEqualTo(original);
        assertThat(json).contains("payments-api", "trace-123", "APPLICATION");
    }
}
