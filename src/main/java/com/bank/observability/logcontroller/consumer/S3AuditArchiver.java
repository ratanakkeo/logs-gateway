package com.bank.observability.logcontroller.consumer;

import com.bank.observability.logcontroller.config.AwsS3Properties;
import com.bank.observability.logcontroller.model.LogPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class S3AuditArchiver {

    private static final DateTimeFormatter KEY_TIME =
            DateTimeFormatter.ofPattern("yyyy/MM/dd/HH").withZone(ZoneOffset.UTC);

    private final S3Client s3Client;
    private final AwsS3Properties awsS3Properties;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.audit}",
            groupId = "log-controller-s3-audit",
            batch = "true"
    )
    public void archive(List<LogPayload> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        byte[] compressed = gzip(toNdjson(batch));
        String key = objectKey(batch);
        log.info("s3_archive_batch size={} key={}", batch.size(), key);
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
    }

    String toNdjson(List<LogPayload> batch) {
        StringBuilder ndjson = new StringBuilder();
        for (LogPayload payload : batch) {
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

    String objectKey(List<LogPayload> batch) {
        Instant now = Instant.now();
        String prefix = awsS3Properties.getS3().getPrefix() == null ? "" : awsS3Properties.getS3().getPrefix();
        String traceOrUuid = batch.getFirst().getTraceId() != null
                ? batch.getFirst().getTraceId()
                : UUID.randomUUID().toString();
        return prefix + KEY_TIME.format(now) + "/" + traceOrUuid + "-" + now.toEpochMilli() + ".ndjson.gz";
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
