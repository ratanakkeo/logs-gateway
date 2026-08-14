package com.bank.observability.logsgateway.audit.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditArchiveService {

    private final AuditArchiveWriter auditArchiveWriter;

    public AuditArchiveService(AuditArchiveWriter auditArchiveWriter) {
        this.auditArchiveWriter = auditArchiveWriter;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.audit}",
            groupId = "log-controller-s3-audit",
            batch = "true"
    )
    public void archive(List<LogPayload> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        auditArchiveWriter.archive(batch);
    }
}
