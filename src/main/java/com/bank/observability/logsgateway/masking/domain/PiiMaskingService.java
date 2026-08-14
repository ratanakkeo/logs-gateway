package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.ingest.api.LogPayload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PiiMaskingService {

    static final String MASKED_PAN = "[MASKED_PAN]";
    private static final Pattern PAN_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");

    public String scrubMessage(String raw) {
        if (raw == null) {
            return null;
        }
        return PAN_PATTERN.matcher(raw).replaceAll(MASKED_PAN);
    }

    public Map<String, Object> scrubData(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Map<String, Object> cleaned = new LinkedHashMap<>();
        data.forEach((key, value) -> cleaned.put(key, scrubValue(value)));
        return cleaned;
    }

    public LogPayload scrub(LogPayload payload) {
        if (payload == null) {
            return null;
        }
        return LogPayload.builder()
                .timestamp(payload.getTimestamp())
                .serviceName(payload.getServiceName())
                .traceId(payload.getTraceId())
                .logLevel(payload.getLogLevel())
                .logType(payload.getLogType())
                .message(scrubMessage(payload.getMessage()))
                .data(scrubData(payload.getData()))
                .build();
    }

    private Object scrubValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return scrubMessage(stringValue);
        }
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> nested = new LinkedHashMap<>();
            mapValue.forEach((nestedKey, nestedValue) ->
                    nested.put(String.valueOf(nestedKey), scrubValue(nestedValue)));
            return nested;
        }
        if (value instanceof List<?> listValue) {
            List<Object> scrubbed = new ArrayList<>(listValue.size());
            for (Object element : listValue) {
                scrubbed.add(scrubValue(element));
            }
            return scrubbed;
        }
        return value;
    }
}
