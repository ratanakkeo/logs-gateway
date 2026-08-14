package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Order(2)
public class CvvMasker implements Masker {

    private static final Set<String> CVV_KEYS = Set.of("cvv", "cvc", "csc", "cid", "securitycode", "cardcvv");

    @Override
    public LogEnvelope mask(LogEnvelope envelope) {
        if (envelope == null) {
            return null;
        }
        return envelope.withData(stripCvvFields(envelope.data()));
    }

    Map<String, Object> stripCvvFields(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Map<String, Object> cleaned = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            if (isCvvKey(key) && isCvvValue(value)) {
                return;
            }
            cleaned.put(key, stripValue(value));
        });
        return cleaned;
    }

    private Object stripValue(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> nested = new LinkedHashMap<>();
            mapValue.forEach((nestedKey, nestedValue) -> nested.put(String.valueOf(nestedKey), nestedValue));
            return stripCvvFields(nested);
        }
        if (value instanceof List<?> listValue) {
            List<Object> stripped = new ArrayList<>(listValue.size());
            for (Object element : listValue) {
                stripped.add(stripValue(element));
            }
            return stripped;
        }
        return value;
    }

    private static boolean isCvvKey(String key) {
        return key != null && CVV_KEYS.contains(key.toLowerCase(Locale.ROOT).replace("_", ""));
    }

    private static boolean isCvvValue(Object value) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof String) && !(value instanceof Number)) {
            return false;
        }
        String digits = String.valueOf(value).replaceAll("\\D", "");
        return digits.length() == 3 || digits.length() == 4;
    }
}
