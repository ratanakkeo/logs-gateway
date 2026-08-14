package com.bank.observability.logsgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public class AwsS3Properties {
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
    private S3 s3 = new S3();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3 == null ? new S3() : s3;
    }

    public static class S3 {
        private String bucket;
        private String prefix;
        private long multipartThresholdBytes = 5_242_880L;
        private long partSizeBytes = 5_242_880L;
        private long flushMaxBytes = 1_048_576L;
        private long flushIntervalMs = 60_000L;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public long getMultipartThresholdBytes() {
            return multipartThresholdBytes;
        }

        public void setMultipartThresholdBytes(long multipartThresholdBytes) {
            this.multipartThresholdBytes = multipartThresholdBytes;
        }

        public long getPartSizeBytes() {
            return partSizeBytes;
        }

        public void setPartSizeBytes(long partSizeBytes) {
            this.partSizeBytes = partSizeBytes;
        }

        public long getFlushMaxBytes() {
            return flushMaxBytes;
        }

        public void setFlushMaxBytes(long flushMaxBytes) {
            this.flushMaxBytes = flushMaxBytes;
        }

        public long getFlushIntervalMs() {
            return flushIntervalMs;
        }

        public void setFlushIntervalMs(long flushIntervalMs) {
            this.flushIntervalMs = flushIntervalMs;
        }
    }
}
