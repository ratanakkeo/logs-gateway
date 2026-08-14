package com.bank.observability.logsgateway.ingest.domain;

import com.bank.observability.logsgateway.masking.domain.MaskingPipeline;
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
    private MaskingPipeline maskingPipeline;

    @Mock
    private TopicRouter topicRouter;

    @Mock
    private LogPublisher logPublisher;

    @Mock
    private IngestMetrics ingestMetrics;

    @InjectMocks
    private LogIngestionService logIngestionService;

    @Test
    void normalizesTimestampMasksAndRoutes() {
        LogEnvelope payload = new LogEnvelope(null, "payments-api", "trace-1", null, "APPLICATION", "ok", null);
        when(maskingPipeline.scrub(any(LogEnvelope.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(topicRouter.resolve(any(LogEnvelope.class))).thenReturn("bank.logs.app");

        logIngestionService.ingestAsync(payload);

        ArgumentCaptor<LogEnvelope> captor = ArgumentCaptor.forClass(LogEnvelope.class);
        verify(maskingPipeline).scrub(captor.capture());
        verify(logPublisher).publish(eq("bank.logs.app"), eq("trace-1"), any(LogEnvelope.class));
        verify(ingestMetrics).recordIngest("APPLICATION");
        assertThat(captor.getValue().timestamp()).isNotNull();
    }
}
