package com.bank.observability.logsgateway.shared.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogsGatewayMetricsTest {

    private SimpleMeterRegistry registry;
    private LogsGatewayMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new LogsGatewayMetrics(registry);
    }

    @Test
    void recordsIngestAndMaskHitsWithTags() {
        metrics.recordIngest("APPLICATION");
        metrics.recordIngest("AUDIT");
        metrics.recordHit("pan");

        assertThat(registry.counter("logs_gateway_ingest", "logType", "APPLICATION").count()).isEqualTo(1);
        assertThat(registry.counter("logs_gateway_ingest", "logType", "AUDIT").count()).isEqualTo(1);
        assertThat(registry.counter("logs_gateway_mask_hits", "type", "pan").count()).isEqualTo(1);
    }

    @Test
    void recordsKafkaFailuresOpenSearchTimerAndS3Bytes() {
        metrics.recordKafkaSendFailure();
        var sample = metrics.startOpenSearchBulk();
        metrics.stopOpenSearchBulk(sample);
        metrics.recordS3FlushBytes(2048);

        assertThat(registry.counter("logs_gateway_kafka_send_failures").count()).isEqualTo(1);
        assertThat(registry.timer("logs_gateway_opensearch_bulk").count()).isEqualTo(1);
        assertThat(registry.summary("logs_gateway_s3_flush_bytes").totalAmount()).isEqualTo(2048);
    }
}
