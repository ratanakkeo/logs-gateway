package com.bank.observability.logsgateway.appindex.infrastructure;

import com.bank.observability.logsgateway.appindex.domain.LogIndexWriter;
import com.bank.observability.logsgateway.config.OpenSearchProperties;
import com.bank.observability.logsgateway.ingest.api.LogPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class OpenSearchHttpClient implements LogIndexWriter {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchHttpClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenSearchProperties properties;

    public OpenSearchHttpClient(HttpClient httpClient,
                                ObjectMapper objectMapper,
                                OpenSearchProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void index(LogPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String endpoint = properties.getEndpoint().replaceAll("/$", "");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/" + properties.getIndex() + "/_doc"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                log.error("opensearch_index_failed status={} body={}", status, response.body());
                throw new IllegalStateException(
                        "OpenSearch indexing failed with HTTP " + status + ": " + response.body());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenSearch indexing interrupted", ex);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("opensearch_index_failed", ex);
            throw new IllegalStateException("OpenSearch indexing failed", ex);
        }
    }
}
