package com.bank.observability.logcontroller.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.opensearch")
public class OpenSearchProperties {
    private String endpoint;
    private String index;
}
