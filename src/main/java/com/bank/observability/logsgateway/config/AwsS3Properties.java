package com.bank.observability.logsgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.aws")
public class AwsS3Properties {
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
    private S3 s3 = new S3();

    @Data
    public static class S3 {
        private String bucket;
        private String prefix;
        private long multipartThresholdBytes = 5_242_880L;
        private long partSizeBytes = 5_242_880L;
    }
}
