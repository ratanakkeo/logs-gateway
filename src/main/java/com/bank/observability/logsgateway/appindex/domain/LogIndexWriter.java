package com.bank.observability.logsgateway.appindex.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;

public interface LogIndexWriter {

    void index(LogPayload payload);
}
