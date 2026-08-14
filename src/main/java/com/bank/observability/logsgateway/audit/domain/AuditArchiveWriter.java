package com.bank.observability.logsgateway.audit.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;

import java.util.List;

public interface AuditArchiveWriter {

    void archive(List<LogPayload> batch);
}
