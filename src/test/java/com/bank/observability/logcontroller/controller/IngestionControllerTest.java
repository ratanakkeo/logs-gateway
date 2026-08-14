package com.bank.observability.logcontroller.controller;

import com.bank.observability.logcontroller.model.LogPayload;
import com.bank.observability.logcontroller.service.LogRoutingService;
import com.bank.observability.logcontroller.service.PiiMaskingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
@Import(IngestionControllerTest.SameThreadExecutorConfig.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PiiMaskingService piiMaskingService;

    @MockitoBean
    private LogRoutingService logRoutingService;

    @Test
    void missingRequiredFieldsReturn400() throws Exception {
        LogPayload payload = LogPayload.builder()
                .message("no identifiers")
                .build();

        mockMvc.perform(post("/v1/logs/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validPayloadReturns202AndHandsOffAsynchronously() throws Exception {
        LogPayload payload = LogPayload.builder()
                .serviceName("payments-api")
                .traceId("trace-1")
                .logType("APPLICATION")
                .message("ok")
                .data(Map.of("k", "v"))
                .build();
        when(piiMaskingService.scrub(any(LogPayload.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/v1/logs/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted());

        verify(piiMaskingService).scrub(any(LogPayload.class));
        verify(logRoutingService).route(any(LogPayload.class));
    }

    @TestConfiguration
    static class SameThreadExecutorConfig {
        @Bean(name = "virtualThreadExecutor")
        Executor virtualThreadExecutor() {
            return Runnable::run;
        }
    }
}
