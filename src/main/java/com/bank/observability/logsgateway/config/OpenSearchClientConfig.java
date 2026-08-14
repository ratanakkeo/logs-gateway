package com.bank.observability.logsgateway.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchClientConfig {

    @Bean
    OpenSearchClient openSearchClient(OpenSearchProperties properties) {
        HttpHost host = HttpHost.create(properties.getEndpoint());
        RestClient restClient = RestClient.builder(host).build();
        return new OpenSearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
    }
}
