package com.bank.observability.logsgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.opensearch")
public class OpenSearchProperties {
    private String endpoint;
    private String index;
    private int bulkSize = 500;
    private long flushIntervalMs = 5_000L;
}
