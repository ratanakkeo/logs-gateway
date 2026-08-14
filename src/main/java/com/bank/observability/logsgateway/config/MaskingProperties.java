package com.bank.observability.logsgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.masking")
public class MaskingProperties {

    private List<String> fields = new ArrayList<>(List.of("password", "pin", "secret"));

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields == null ? List.of() : fields;
    }
}
