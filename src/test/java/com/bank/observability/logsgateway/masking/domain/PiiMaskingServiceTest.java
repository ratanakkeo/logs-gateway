package com.bank.observability.logsgateway.masking.domain;

import com.bank.observability.logsgateway.config.MaskingProperties;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskingServiceTest {

    private final PanMasker panMasker = new PanMasker();

    @Test
    void scrubMessageMasksPanInText() {
        String masked = panMasker.maskText("card 4111111111111111 charged");
        assertThat(masked).isEqualTo("card 411111******1111 charged");
    }

    @Test
    void scrubDataMasksNestedPan() {
        LogEnvelope envelope = new LogEnvelope(null, null, null, null, "APPLICATION", null, Map.of(
                "card", Map.of("pan", "4111-1111-1111-1111"),
                "notes", List.of("acct 5500000000000004")
        ));

        LogEnvelope cleaned = panMasker.mask(envelope);

        assertThat(cleaned.data().get("card")).isEqualTo(Map.of("pan", "411111******1111"));
        assertThat(cleaned.data().get("notes")).isEqualTo(List.of("acct 550000******0004"));
    }

    @Test
    void shortDigitSequencesAreLeftUntouched() {
        String message = "otp 123456 and ref 123456789012";
        assertThat(panMasker.maskText(message)).isEqualTo(message);
    }

    @Test
    void scrubReturnsCopyWithoutPan() {
        LogEnvelope payload = new LogEnvelope(
                null, null, null, null, "APPLICATION", "PAN 4111111111111111", Map.of("pan", "4111111111111111"));

        LogEnvelope scrubbed = panMasker.mask(payload);

        assertThat(scrubbed.message()).isEqualTo("PAN 411111******1111");
        assertThat(scrubbed.data()).containsEntry("pan", "411111******1111");
        assertThat(payload.message()).isEqualTo("PAN 4111111111111111");
    }

    @Test
    void cvvFieldsAreStripped() {
        CvvMasker cvvMasker = new CvvMasker();
        LogEnvelope envelope = new LogEnvelope(null, null, null, null, "APPLICATION", "ok",
                Map.of("cvv", "123", "amount", "10.00"));

        assertThat(cvvMasker.mask(envelope).data()).containsOnlyKeys("amount");
    }

    @Test
    void configFieldsAreMasked() {
        MaskingProperties properties = new MaskingProperties();
        properties.setFields(List.of("password"));
        ConfigFieldMasker masker = new ConfigFieldMasker(properties);
        LogEnvelope envelope = new LogEnvelope(null, null, null, null, "APPLICATION", "ok",
                Map.of("password", "secret", "user", "ada"));

        assertThat(masker.mask(envelope).data())
                .containsEntry("password", "[MASKED]")
                .containsEntry("user", "ada");
    }

    @Test
    void chainRunsPanThenCvvThenConfig() {
        MaskingProperties properties = new MaskingProperties();
        properties.setFields(List.of("pin"));
        MaskingPipeline pipeline = new MaskingPipeline(List.of(
                new PanMasker(), new CvvMasker(), new ConfigFieldMasker(properties)));
        LogEnvelope envelope = new LogEnvelope(null, null, null, null, "APPLICATION",
                "card 4111111111111111", Map.of("cvv", "123", "pin", "9999"));

        LogEnvelope scrubbed = pipeline.scrub(envelope);

        assertThat(scrubbed.message()).isEqualTo("card 411111******1111");
        assertThat(scrubbed.data()).doesNotContainKey("cvv");
        assertThat(scrubbed.data()).containsEntry("pin", "[MASKED]");
    }
}
