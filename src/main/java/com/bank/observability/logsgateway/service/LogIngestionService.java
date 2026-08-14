package com.bank.observability.logsgateway.service;

import com.bank.observability.logsgateway.model.LogPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
public class LogIngestionService {

    private static final Logger log = LoggerFactory.getLogger(LogIngestionService.class);

    private final PiiMaskingService piiMaskingService;
    private final LogRoutingService logRoutingService;

    public LogIngestionService(PiiMaskingService piiMaskingService, LogRoutingService logRoutingService) {
        this.piiMaskingService = piiMaskingService;
        this.logRoutingService = logRoutingService;
    }

    @Async("virtualThreadExecutor")
    public void ingestAsync(LogPayload payload) {
        Instant utc = payload.getTimestamp() == null
                ? Instant.now()
                : payload.getTimestamp().atZone(ZoneOffset.UTC).toInstant();
        payload.setTimestamp(utc);
        LogPayload scrubbed = piiMaskingService.scrub(payload);
        logRoutingService.route(scrubbed);
        log.info("log_ingested serviceName={} logType={} traceId={}",
                scrubbed.getServiceName(), scrubbed.getLogType(), scrubbed.getTraceId());
    }
}
