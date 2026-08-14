package com.bank.observability.logcontroller.service;

import com.bank.observability.logcontroller.model.LogPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskingServiceTest {

    private final PiiMaskingService service = new PiiMaskingService();

    @Test
    void scrubMessageMasksPanInText() {
        String masked = service.scrubMessage("card 4111111111111111 charged");
        assertThat(masked).isEqualTo("card [MASKED_PAN] charged");
    }

    @Test
    void scrubDataMasksNestedPan() {
        Map<String, Object> data = Map.of(
                "card", Map.of("pan", "4111-1111-1111-1111"),
                "notes", List.of("acct 5500000000000004")
        );

        Map<String, Object> cleaned = service.scrubData(data);

        assertThat(cleaned.get("card")).isEqualTo(Map.of("pan", "[MASKED_PAN]"));
        assertThat(cleaned.get("notes")).isEqualTo(List.of("acct [MASKED_PAN]"));
    }

    @Test
    void shortDigitSequencesAreLeftUntouched() {
        String message = "otp 123456 and ref 123456789012";
        assertThat(service.scrubMessage(message)).isEqualTo(message);
    }

    @Test
    void scrubReturnsCopyWithoutPan() {
        LogPayload payload = LogPayload.builder()
                .message("PAN 4111111111111111")
                .data(Map.of("pan", "4111111111111111"))
                .build();

        LogPayload scrubbed = service.scrub(payload);

        assertThat(scrubbed.getMessage()).isEqualTo("PAN [MASKED_PAN]");
        assertThat(scrubbed.getData()).containsEntry("pan", "[MASKED_PAN]");
        assertThat(payload.getMessage()).isEqualTo("PAN 4111111111111111");
    }
}
