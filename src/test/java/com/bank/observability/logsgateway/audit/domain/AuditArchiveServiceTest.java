package com.bank.observability.logsgateway.audit.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditArchiveServiceTest {

    @Mock
    private AuditArchiveWriter auditArchiveWriter;

    @InjectMocks
    private AuditArchiveService auditArchiveService;

    @Test
    void delegatesNonEmptyBatchToWriter() {
        List<LogPayload> batch = List.of(LogPayload.builder().traceId("t1").logType("AUDIT").build());

        auditArchiveService.archive(batch);

        verify(auditArchiveWriter).archive(batch);
    }

    @Test
    void ignoresEmptyBatch() {
        auditArchiveService.archive(List.of());

        verify(auditArchiveWriter, never()).archive(List.of());
    }
}
