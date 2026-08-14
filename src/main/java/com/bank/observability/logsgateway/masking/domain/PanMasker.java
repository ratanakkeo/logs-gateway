package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(1)
public class PanMasker implements Masker {

    private static final Pattern PAN_CANDIDATE = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");

    @Override
    public String type() {
        return "pan";
    }

    @Override
    public LogEnvelope mask(LogEnvelope envelope) {
        if (envelope == null) {
            return null;
        }
        return envelope.withMessage(maskText(envelope.message()))
                .withData(maskData(envelope.data()));
    }

    String maskText(String raw) {
        if (raw == null) {
            return null;
        }
        Matcher matcher = PAN_CANDIDATE.matcher(raw);
        StringBuilder masked = new StringBuilder();
        while (matcher.find()) {
            String digits = matcher.group().replaceAll("[^0-9]", "");
            if (passesLuhn(digits)) {
                matcher.appendReplacement(masked, Matcher.quoteReplacement(retainFirst6Last4(digits)));
            }
        }
        matcher.appendTail(masked);
        return masked.toString();
    }

    Object maskValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return maskText(stringValue);
        }
        if (value instanceof Map<?, ?> mapValue) {
            return maskData(castMap(mapValue));
        }
        if (value instanceof List<?> listValue) {
            List<Object> masked = new ArrayList<>(listValue.size());
            for (Object element : listValue) {
                masked.add(maskValue(element));
            }
            return masked;
        }
        return value;
    }

    private Map<String, Object> maskData(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Map<String, Object> cleaned = new LinkedHashMap<>();
        data.forEach((key, value) -> cleaned.put(key, maskValue(value)));
        return cleaned;
    }

    private static Map<String, Object> castMap(Map<?, ?> mapValue) {
        Map<String, Object> nested = new LinkedHashMap<>();
        mapValue.forEach((nestedKey, nestedValue) -> nested.put(String.valueOf(nestedKey), nestedValue));
        return nested;
    }

    static String retainFirst6Last4(String digits) {
        int hidden = digits.length() - 10;
        return digits.substring(0, 6) + "*".repeat(hidden) + digits.substring(digits.length() - 4);
    }

    static boolean passesLuhn(String digits) {
        if (digits == null || digits.length() < 13 || digits.length() > 19) {
            return false;
        }
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                return false;
            }
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
