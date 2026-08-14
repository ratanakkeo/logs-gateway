package com.bank.observability.logsgateway.ingest.domain;

import com.bank.observability.logsgateway.masking.domain.MaskingPipeline;
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

    private final MaskingPipeline maskingPipeline;
    private final TopicRouter topicRouter;
    private final LogPublisher logPublisher;

    public LogIngestionService(MaskingPipeline maskingPipeline,
                               TopicRouter topicRouter,
                               LogPublisher logPublisher) {
        this.maskingPipeline = maskingPipeline;
        this.topicRouter = topicRouter;
        this.logPublisher = logPublisher;
    }

    @Async("virtualThreadExecutor")
    public void ingestAsync(LogEnvelope payload) {
        Instant utc = payload.timestamp() == null
                ? Instant.now()
                : payload.timestamp().atZone(ZoneOffset.UTC).toInstant();
        LogEnvelope normalized = payload.withTimestamp(utc);
        LogEnvelope scrubbed = maskingPipeline.scrub(normalized);
        String topic = topicRouter.resolve(scrubbed);
        logPublisher.publish(topic, scrubbed.traceId(), scrubbed);
        log.info("log_ingested serviceName={} logType={} traceId={}",
                scrubbed.serviceName(), scrubbed.logType(), scrubbed.traceId());
    }
}
