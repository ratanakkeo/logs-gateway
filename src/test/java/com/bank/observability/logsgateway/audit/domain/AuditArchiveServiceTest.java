package com.bank.observability.logsgateway.audit.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditArchiveServiceTest {

    @Mock
    private AuditArchiveWriter auditArchiveWriter;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private AuditArchiveService auditArchiveService;

    @Test
    void delegatesNonEmptyBatchToWriter() {
        List<LogEnvelope> batch = List.of(
                new LogEnvelope(null, null, "t1", null, "AUDIT", null, null));

        auditArchiveService.archive(batch, acknowledgment);

        verify(auditArchiveWriter).archive(batch);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void ignoresEmptyBatch() {
        auditArchiveService.archive(List.of(), acknowledgment);

        verify(auditArchiveWriter, never()).archive(List.of());
    }
}
