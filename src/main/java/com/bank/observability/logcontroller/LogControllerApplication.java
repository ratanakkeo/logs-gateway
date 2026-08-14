package com.bank.observability.logcontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LogControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogControllerApplication.class, args);
    }
}
