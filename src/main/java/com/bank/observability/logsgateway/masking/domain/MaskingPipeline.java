package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaskingPipeline {

    private final List<Masker> maskers;

    public MaskingPipeline(List<Masker> maskers) {
        this.maskers = List.copyOf(maskers);
    }

    public LogEnvelope scrub(LogEnvelope envelope) {
        LogEnvelope current = envelope;
        for (Masker masker : maskers) {
            current = masker.mask(current);
        }
        return current;
    }
}
