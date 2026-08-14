package com.bank.observability.logsgateway.ingest.domain;

public interface IngestMetrics {

    void recordIngest(String logType);
}
