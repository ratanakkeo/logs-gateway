package com.bank.observability.logsgateway.appindex.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppLogIndexerTest {

    @Mock
    private LogIndexWriter logIndexWriter;

    @InjectMocks
    private AppLogIndexer indexer;

    @Test
    void postsPayloadToOpenSearchClient() {
        LogPayload payload = LogPayload.builder()
                .serviceName("auth-api")
                .traceId("trace-os")
                .logType("APPLICATION")
                .message("indexed")
                .build();

        indexer.index(payload);

        verify(logIndexWriter).index(payload);
    }
}
