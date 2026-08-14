package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.config.MaskingProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(3)
public class ConfigFieldMasker implements Masker {

    static final String MASKED = "[MASKED]";

    private final Set<String> fields;

    public ConfigFieldMasker(MaskingProperties properties) {
        this.fields = properties.getFields().stream()
                .map(field -> field.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public LogEnvelope mask(LogEnvelope envelope) {
        if (envelope == null) {
            return null;
        }
        return envelope.withData(maskData(envelope.data()));
    }

    Map<String, Object> maskData(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Map<String, Object> cleaned = new LinkedHashMap<>();
        data.forEach((key, value) -> cleaned.put(key, maskValue(key, value)));
        return cleaned;
    }

    private Object maskValue(String key, Object value) {
        if (key != null && fields.contains(key.toLowerCase(Locale.ROOT))) {
            return MASKED;
        }
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> nested = new LinkedHashMap<>();
            mapValue.forEach((nestedKey, nestedValue) -> nested.put(String.valueOf(nestedKey), nestedValue));
            return maskData(nested);
        }
        if (value instanceof List<?> listValue) {
            List<Object> masked = new ArrayList<>(listValue.size());
            for (Object element : listValue) {
                masked.add(maskValue(null, element));
            }
            return masked;
        }
        return value;
    }
}
