package com.bank.observability.logsgateway.audit.infrastructure;

import com.bank.observability.logsgateway.audit.domain.AuditArchiveWriter;
import com.bank.observability.logsgateway.config.AwsS3Properties;
import com.bank.observability.logsgateway.config.DownstreamGuard;
import com.bank.observability.logsgateway.ingest.domain.LogEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Component
public class S3AuditArchiver implements AuditArchiveWriter {

    private static final Logger log = LoggerFactory.getLogger(S3AuditArchiver.class);
    private static final DateTimeFormatter PARTITION_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final S3Client s3Client;
    private final AwsS3Properties awsS3Properties;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final List<LogEnvelope> buffer = new ArrayList<>();

    public S3AuditArchiver(
            S3Client s3Client,
            AwsS3Properties awsS3Properties,
            ObjectMapper objectMapper,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this(s3Client, awsS3Properties, objectMapper, retryRegistry.retry("s3"),
                circuitBreakerRegistry.circuitBreaker("s3"));
    }

    S3AuditArchiver(
            S3Client s3Client,
            AwsS3Properties awsS3Properties,
            ObjectMapper objectMapper,
            Retry retry,
            CircuitBreaker circuitBreaker) {
        this.s3Client = s3Client;
        this.awsS3Properties = awsS3Properties;
        this.objectMapper = objectMapper;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public synchronized void archive(List<LogEnvelope> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        buffer.addAll(batch);
        if (estimatedSize() >= awsS3Properties.getS3().getFlushMaxBytes()) {
            flush();
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "${app.aws.s3.flush-interval-ms:60000}")
    public synchronized void scheduledFlush() {
        flush();
    }

    synchronized void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        List<LogEnvelope> snapshot = List.copyOf(buffer);
        buffer.clear();
        byte[] compressed = gzip(toNdjson(snapshot));
        String key = objectKey(snapshot);
        log.info("s3_archive_batch size={} key={}", snapshot.size(), key);
        DownstreamGuard.run(retry, circuitBreaker, () -> {
            if (compressed.length >= awsS3Properties.getS3().getMultipartThresholdBytes()) {
                multipartUpload(key, compressed);
            } else {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(awsS3Properties.getS3().getBucket())
                                .key(key)
                                .contentType("application/gzip")
                                .build(),
                        RequestBody.fromBytes(compressed)
                );
            }
        });
    }

    private long estimatedSize() {
        return toNdjson(buffer).getBytes(StandardCharsets.UTF_8).length;
    }

    String toNdjson(List<LogEnvelope> batch) {
        StringBuilder ndjson = new StringBuilder();
        for (LogEnvelope payload : batch) {
            try {
                ndjson.append(objectMapper.writeValueAsString(payload)).append('\n');
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to serialize audit log payload", ex);
            }
        }
        return ndjson.toString();
    }

    byte[] gzip(String ndjson) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(ndjson.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            return bytes.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to gzip audit batch", ex);
        }
    }

    String objectKey(List<LogEnvelope> batch) {
        Instant now = Instant.now();
        String prefix = awsS3Properties.getS3().getPrefix() == null ? "audit/" : awsS3Properties.getS3().getPrefix();
        String service = batch.getFirst().serviceName() == null || batch.getFirst().serviceName().isBlank()
                ? "unknown"
                : batch.getFirst().serviceName();
        return prefix + "dt=" + PARTITION_DATE.format(now) + "/service=" + service + "/"
                + UUID.randomUUID() + ".jsonl.gz";
    }

    private void multipartUpload(String key, byte[] compressed) {
        String bucket = awsS3Properties.getS3().getBucket();
        int partSize = Math.toIntExact(awsS3Properties.getS3().getPartSizeBytes());
        CreateMultipartUploadResponse created = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("application/gzip")
                        .build()
        );
        String uploadId = created.uploadId();
        try {
            List<CompletedPart> parts = new ArrayList<>();
            int partNumber = 1;
            for (int offset = 0; offset < compressed.length; offset += partSize) {
                int end = Math.min(offset + partSize, compressed.length);
                byte[] partBytes = Arrays.copyOfRange(compressed, offset, end);
                UploadPartResponse response = s3Client.uploadPart(
                        UploadPartRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .build(),
                        RequestBody.fromBytes(partBytes)
                );
                parts.add(CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(response.eTag())
                        .build());
                partNumber++;
            }
            s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .uploadId(uploadId)
                            .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
                            .build()
            );
        } catch (RuntimeException ex) {
            try {
                s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .build());
            } catch (RuntimeException abortEx) {
                log.warn("Failed to abort multipart upload {}", uploadId, abortEx);
            }
            throw ex;
        }
    }
}
