package com.bank.observability.logcontroller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        var builder = S3Client.builder().region(Region.of(properties.getRegion()));
        if (StringUtils.hasText(properties.getAccessKeyId())
                && StringUtils.hasText(properties.getSecretAccessKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            properties.getAccessKeyId(),
                            properties.getSecretAccessKey()
                    )
            ));
        }
        return builder.build();
    }
}
