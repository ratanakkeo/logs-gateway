package com.bank.observability.logcontroller.consumer;

import com.bank.observability.logcontroller.model.LogPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenSearchIndexerTest {

    @Mock
    private OpenSearchHttpClient openSearchHttpClient;

    @InjectMocks
    private OpenSearchIndexer indexer;

    @Test
    void postsPayloadToOpenSearchClient() {
        LogPayload payload = LogPayload.builder()
                .serviceName("auth-api")
                .traceId("trace-os")
                .logType("APPLICATION")
                .message("indexed")
                .build();

        indexer.index(payload);

        verify(openSearchHttpClient).index(payload);
    }
}
