package com.bank.observability.logsgateway.masking.domain;

public interface MaskingMetrics {

    void recordHit(String type);
}
