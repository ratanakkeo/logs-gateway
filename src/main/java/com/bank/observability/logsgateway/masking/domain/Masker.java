package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;

public interface Masker {

    LogEnvelope mask(LogEnvelope envelope);
}
