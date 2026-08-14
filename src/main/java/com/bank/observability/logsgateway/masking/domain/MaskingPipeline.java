package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MaskingPipeline {

    private final List<Masker> maskers;
    private final MaskingMetrics maskingMetrics;

    public MaskingPipeline(List<Masker> maskers, MaskingMetrics maskingMetrics) {
        this.maskers = List.copyOf(maskers);
        this.maskingMetrics = maskingMetrics;
    }

    public LogEnvelope scrub(LogEnvelope envelope) {
        LogEnvelope current = envelope;
        for (Masker masker : maskers) {
            LogEnvelope next = masker.mask(current);
            if (next != null && !Objects.equals(next, current)) {
                maskingMetrics.recordHit(masker.type());
            }
            current = next;
        }
        return current;
    }
}
