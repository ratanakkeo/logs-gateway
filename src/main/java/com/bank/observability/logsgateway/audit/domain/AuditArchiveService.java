package com.bank.observability.logsgateway.audit.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
    public void archive(List<LogEnvelope> batch, Acknowledgment acknowledgment) {
        if (batch == null || batch.isEmpty()) {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }
        auditArchiveWriter.archive(batch);
        acknowledgment.acknowledge();
    }
}
