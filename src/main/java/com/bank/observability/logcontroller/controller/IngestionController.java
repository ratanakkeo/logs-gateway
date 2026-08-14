package com.bank.observability.logcontroller.controller;

import com.bank.observability.logcontroller.model.LogPayload;
import com.bank.observability.logcontroller.service.LogRoutingService;
import com.bank.observability.logcontroller.service.PiiMaskingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/v1/logs")
public class IngestionController {

    private final PiiMaskingService piiMaskingService;
    private final LogRoutingService logRoutingService;
    private final Executor virtualThreadExecutor;

    public IngestionController(
            PiiMaskingService piiMaskingService,
            LogRoutingService logRoutingService,
            @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.piiMaskingService = piiMaskingService;
        this.logRoutingService = logRoutingService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @PostMapping("/ingest")
    public ResponseEntity<?> ingest(@RequestBody LogPayload payload) {
        if (payload == null
                || !StringUtils.hasText(payload.getTraceId())
                || !StringUtils.hasText(payload.getServiceName())
                || !StringUtils.hasText(payload.getLogType())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "traceId, serviceName, and logType are required"
            ));
        }

        Instant utc = payload.getTimestamp() == null
                ? Instant.now()
                : payload.getTimestamp().atZone(ZoneOffset.UTC).toInstant();
        payload.setTimestamp(utc);

        virtualThreadExecutor.execute(() -> {
            LogPayload scrubbed = piiMaskingService.scrub(payload);
            logRoutingService.route(scrubbed);
        });

        return ResponseEntity.accepted().build();
    }
}
