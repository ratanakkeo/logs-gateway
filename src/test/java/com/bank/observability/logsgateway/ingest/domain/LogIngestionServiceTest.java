package com.bank.observability.logsgateway.ingest.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import com.bank.observability.logsgateway.masking.domain.PiiMaskingService;
import com.bank.observability.logsgateway.routing.domain.TopicRouter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogIngestionServiceTest {

    @Mock
    private PiiMaskingService piiMaskingService;

    @Mock
    private TopicRouter topicRouter;

    @Mock
    private LogPublisher logPublisher;

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
        when(topicRouter.resolve(any(LogPayload.class))).thenReturn("bank.logs.app");

        logIngestionService.ingestAsync(payload);

        ArgumentCaptor<LogPayload> captor = ArgumentCaptor.forClass(LogPayload.class);
        verify(piiMaskingService).scrub(captor.capture());
        verify(logPublisher).publish(eq("bank.logs.app"), eq("trace-1"), any(LogPayload.class));
        assertThat(captor.getValue().getTimestamp()).isNotNull();
    }
}
