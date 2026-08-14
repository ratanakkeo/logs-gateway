package com.bank.observability.logsgateway.appindex.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AppLogIndexer {

    private static final Logger log = LoggerFactory.getLogger(AppLogIndexer.class);

    private final LogIndexWriter logIndexWriter;

    public AppLogIndexer(LogIndexWriter logIndexWriter) {
        this.logIndexWriter = logIndexWriter;
    }

    @KafkaListener(topics = "${app.kafka.topics.app}", groupId = "log-controller-opensearch")
    public void index(LogPayload payload) {
        log.info("opensearch_index serviceName={} traceId={}", payload.getServiceName(), payload.getTraceId());
        logIndexWriter.index(payload);
    }
}
