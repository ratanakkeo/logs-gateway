package com.bank.observability.logsgateway.shared.metrics;

import com.bank.observability.logsgateway.ingest.domain.IngestMetrics;
import com.bank.observability.logsgateway.masking.domain.MaskingMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class LogsGatewayMetrics implements IngestMetrics, MaskingMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter kafkaSendFailures;
    private final Timer openSearchBulk;
    private final DistributionSummary s3FlushBytes;

    public LogsGatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.kafkaSendFailures = Counter.builder("logs_gateway_kafka_send_failures")
                .description("Kafka send failures reported by whenComplete")
                .register(meterRegistry);
        this.openSearchBulk = Timer.builder("logs_gateway_opensearch_bulk")
                .description("OpenSearch bulk flush duration")
                .register(meterRegistry);
        this.s3FlushBytes = DistributionSummary.builder("logs_gateway_s3_flush_bytes")
                .description("Compressed S3 audit payload size on flush")
                .baseUnit("bytes")
                .register(meterRegistry);
    }

    @Override
    public void recordIngest(String logType) {
        meterRegistry.counter("logs_gateway_ingest", "logType", logType == null || logType.isBlank() ? "unknown" : logType)
                .increment();
    }

    @Override
    public void recordHit(String type) {
        meterRegistry.counter("logs_gateway_mask_hits", "type", type).increment();
    }

    public void recordKafkaSendFailure() {
        kafkaSendFailures.increment();
    }

    public Timer.Sample startOpenSearchBulk() {
        return Timer.start(meterRegistry);
    }

    public void stopOpenSearchBulk(Timer.Sample sample) {
        sample.stop(openSearchBulk);
    }

    public void recordS3FlushBytes(long bytes) {
        s3FlushBytes.record(bytes);
    }
}
