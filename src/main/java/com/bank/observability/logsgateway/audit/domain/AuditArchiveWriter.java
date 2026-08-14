package com.bank.observability.logsgateway.audit.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;

import java.util.List;

public interface AuditArchiveWriter {

    void archive(List<LogEnvelope> batch);
}
