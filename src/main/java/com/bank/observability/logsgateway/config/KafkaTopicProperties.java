package com.bank.observability.logsgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicProperties {
    private String app;
    private String audit;
}
