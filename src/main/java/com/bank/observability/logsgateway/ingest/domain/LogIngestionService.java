package com.bank.observability.logsgateway.ingest.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import com.bank.observability.logsgateway.masking.domain.PiiMaskingService;
import com.bank.observability.logsgateway.routing.domain.TopicRouter;
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
    private final TopicRouter topicRouter;
    private final LogPublisher logPublisher;

    public LogIngestionService(PiiMaskingService piiMaskingService,
                               TopicRouter topicRouter,
                               LogPublisher logPublisher) {
        this.piiMaskingService = piiMaskingService;
        this.topicRouter = topicRouter;
        this.logPublisher = logPublisher;
    }

    @Async("virtualThreadExecutor")
    public void ingestAsync(LogPayload payload) {
        Instant utc = payload.getTimestamp() == null
                ? Instant.now()
                : payload.getTimestamp().atZone(ZoneOffset.UTC).toInstant();
        payload.setTimestamp(utc);
        LogPayload scrubbed = piiMaskingService.scrub(payload);
        String topic = topicRouter.resolve(scrubbed);
        logPublisher.publish(topic, scrubbed.getTraceId(), scrubbed);
        log.info("log_ingested serviceName={} logType={} traceId={}",
                scrubbed.getServiceName(), scrubbed.getLogType(), scrubbed.getTraceId());
    }
}
