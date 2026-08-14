package com.bank.observability.logsgateway.appindex.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;

public interface LogIndexWriter {

    void index(LogEnvelope payload);
}
