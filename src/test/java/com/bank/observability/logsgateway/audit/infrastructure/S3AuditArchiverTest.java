package com.bank.observability.logsgateway.audit.infrastructure;

import com.bank.observability.logsgateway.config.AwsS3Properties;
import com.bank.observability.logsgateway.ingest.api.LogPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3AuditArchiverTest {

    @Mock
    private S3Client s3Client;

    private S3AuditArchiver archiver;
    private AwsS3Properties properties;

    @BeforeEach
    void setUp() {
        properties = new AwsS3Properties();
        properties.setRegion("ap-southeast-1");
        properties.getS3().setBucket("bank-audit-logs-worm");
        properties.getS3().setPrefix("audit/");
        properties.getS3().setMultipartThresholdBytes(5_242_880L);
        properties.getS3().setPartSizeBytes(5_242_880L);
        archiver = new S3AuditArchiver(s3Client, properties, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void smallBatchUsesPutObject() {
        LogPayload payload = LogPayload.builder()
                .traceId("trace-s3")
                .logType("AUDIT")
                .message("teller override")
                .build();

        archiver.archive(List.of(payload));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("bank-audit-logs-worm");
        assertThat(request.key()).startsWith("audit/");
        assertThat(request.key()).contains("trace-s3");
        assertThat(request.key()).endsWith(".ndjson.gz");
        assertThat(request.contentType()).isEqualTo("application/gzip");
    }

    @Test
    void largeBatchUsesMultipartUpload() {
        properties.getS3().setMultipartThresholdBytes(10L);
        properties.getS3().setPartSizeBytes(1024L);
        when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-1").build());
        when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
                .thenReturn(UploadPartResponse.builder().eTag("etag-1").build());

        LogPayload payload = LogPayload.builder()
                .traceId("trace-mp")
                .logType("AUDIT")
                .message("large audit event")
                .build();

        archiver.archive(List.of(payload));

        verify(s3Client).createMultipartUpload(any(CreateMultipartUploadRequest.class));
        verify(s3Client).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
        verify(s3Client).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));
    }
}
