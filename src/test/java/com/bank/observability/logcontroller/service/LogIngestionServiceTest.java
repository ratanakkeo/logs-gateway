package com.bank.observability.logcontroller.service;

import com.bank.observability.logcontroller.model.LogPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogIngestionServiceTest {

    @Mock
    private PiiMaskingService piiMaskingService;

    @Mock
    private LogRoutingService logRoutingService;

    @InjectMocks
    private LogIngestionService logIngestionService;

    @Test
    void normalizesTimestampMasksAndRoutes() {
        LogPayload payload = LogPayload.builder()
                .serviceName("payments-api")
                .traceId("trace-1")
                .logType("APPLICATION")
                .message("ok")
                .build();
        when(piiMaskingService.scrub(any(LogPayload.class))).thenAnswer(invocation -> invocation.getArgument(0));

        logIngestionService.ingestAsync(payload);

        ArgumentCaptor<LogPayload> captor = ArgumentCaptor.forClass(LogPayload.class);
        verify(piiMaskingService).scrub(captor.capture());
        verify(logRoutingService).route(any(LogPayload.class));
        assertThat(captor.getValue().getTimestamp()).isNotNull();
    }
}
