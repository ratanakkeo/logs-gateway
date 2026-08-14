package com.bank.observability.logsgateway.ingest.api;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import com.bank.observability.logsgateway.ingest.domain.LogIngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LogIngestionService logIngestionService;

    @Test
    void missingRequiredFieldsReturn400() throws Exception {
        IngestLogRequest payload = new IngestLogRequest(
                null, null, null, null, null, "no identifiers", null);

        mockMvc.perform(post("/v1/logs/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid log payload"));
    }

    @Test
    void validPayloadReturns202AndHandsOffAsynchronously() throws Exception {
        IngestLogRequest payload = new IngestLogRequest(
                null, "payments-api", "trace-1", null, "APPLICATION", "ok", Map.of("k", "v"));

        mockMvc.perform(post("/v1/logs/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted());

        verify(logIngestionService).ingestAsync(any(LogEnvelope.class));
    }
}
