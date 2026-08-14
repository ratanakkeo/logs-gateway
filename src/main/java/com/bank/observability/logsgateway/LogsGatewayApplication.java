package com.bank.observability.logsgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LogsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogsGatewayApplication.class, args);
    }
}
