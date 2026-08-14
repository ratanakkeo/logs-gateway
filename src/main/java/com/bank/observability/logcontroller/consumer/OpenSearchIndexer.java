package com.bank.observability.logcontroller.consumer;

import com.bank.observability.logcontroller.model.LogPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenSearchIndexer {

    private final OpenSearchHttpClient openSearchHttpClient;

    @KafkaListener(topics = "${app.kafka.topics.app}", groupId = "log-controller-opensearch")
    public void index(LogPayload payload) {
        openSearchHttpClient.index(payload);
    }
}
