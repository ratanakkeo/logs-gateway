package com.bank.observability.logsgateway.appindex.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppLogIndexerTest {

    @Mock
    private LogIndexWriter logIndexWriter;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private AppLogIndexer indexer;

    @Test
    void postsPayloadToOpenSearchClient() {
        LogEnvelope payload = new LogEnvelope(null, "auth-api", "trace-os", null, "APPLICATION", "indexed", null);

        indexer.index(payload, acknowledgment);

        verify(logIndexWriter).index(payload);
        verify(acknowledgment).acknowledge();
    }
}
