# Current structure

**Last Updated:** 2026-08-14

Live layout is **package-by-feature** under `com.bank.observability.logsgateway`. This replaces the pre-refactor `com.bank.observability.logcontroller` / package-by-layer snapshot.

Codemaps: [CODEMAPS/INDEX.md](CODEMAPS/INDEX.md).

There is no `HttpClientConfig`, no `controller/` / `consumer/` / `model/` / `service/` layer packages, and no `target/` dump here.

## Source tree (`src/main/java`)

```
src/main/java/com/bank/observability/logsgateway/
├── LogsGatewayApplication.java
├── appindex/
│   ├── domain/
│   │   ├── AppLogIndexer.java
│   │   └── LogIndexWriter.java
│   └── infrastructure/
│       └── OpenSearchBulkIndexer.java
├── audit/
│   ├── domain/
│   │   ├── AuditArchiveService.java
│   │   └── AuditArchiveWriter.java
│   └── infrastructure/
│       └── S3AuditArchiver.java
├── config/
│   ├── AsyncConfig.java
│   ├── AwsS3Properties.java
│   ├── DownstreamGuard.java
│   ├── KafkaConsumerConfig.java
│   ├── KafkaProducerConfig.java
│   ├── KafkaTopicProperties.java
│   ├── MaskingProperties.java
│   ├── OpenSearchClientConfig.java
│   ├── OpenSearchProperties.java
│   ├── S3ClientConfig.java
│   ├── SecurityConfig.java
│   └── SecurityProperties.java
├── ingest/
│   ├── api/
│   │   ├── IngestLogRequest.java
│   │   └── IngestionController.java
│   ├── domain/
│   │   ├── IngestMetrics.java
│   │   ├── LogEnvelope.java
│   │   ├── LogIngestionService.java
│   │   └── LogPublisher.java
│   └── infrastructure/
│       └── KafkaLogPublisher.java
├── masking/
│   └── domain/
│       ├── ConfigFieldMasker.java
│       ├── CvvMasker.java
│       ├── Masker.java
│       ├── MaskingMetrics.java
│       ├── MaskingPipeline.java
│       └── PanMasker.java
├── routing/
│   └── domain/
│       └── TopicRouter.java
└── shared/
    ├── error/
    │   └── GlobalExceptionHandler.java
    ├── metrics/
    │   └── LogsGatewayMetrics.java
    └── web/
        ├── ApiKeyAuthFilter.java
        ├── RateLimitFilter.java
        └── RequestLoggingFilter.java
```

## Test tree (`src/test/java`)

Tests live under the same package path. Not every main class has a dedicated `*Test` (config beans, some filters, interfaces).

```
src/test/java/com/bank/observability/logsgateway/
├── appindex/
│   ├── domain/
│   │   └── AppLogIndexerTest.java
│   └── infrastructure/
│       ├── KafkaAppLogAckIT.java          # @Tag("it") — excluded from mvn verify
│       └── OpenSearchBulkIndexerTest.java
├── audit/
│   ├── domain/
│   │   └── AuditArchiveServiceTest.java
│   └── infrastructure/
│       └── S3AuditArchiverTest.java
├── ingest/
│   ├── api/
│   │   ├── IngestLogRequestTest.java
│   │   └── IngestionControllerTest.java
│   ├── domain/
│   │   └── LogIngestionServiceTest.java
│   └── infrastructure/
│       └── KafkaLogPublisherTest.java
├── masking/
│   └── domain/
│       └── MaskingPipelineTest.java       # also covers PanMasker, CvvMasker, ConfigFieldMasker
├── routing/
│   └── domain/
│       └── TopicRouterTest.java
└── shared/
    ├── metrics/
    │   └── LogsGatewayMetricsTest.java
    └── web/
        ├── ApiKeyAuthFilterTest.java
        └── RateLimitFilterTest.java
```

`mvn verify` = unit tests (Surefire `excludedGroups=it`). Kafka Testcontainers IT: `mvn test -Dgroups=it`.

## Resources

```
src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── application-test.yml
└── logback-spring.xml
```

## Infra and ops (repo root)

```
Dockerfile
docker-compose.yml          # kafka, opensearch, dashboards, localstack, app
infra/
├── README.md
├── opensearch/
│   ├── index-template.json
│   └── ism-policy.json
└── s3/
    └── lifecycle.json
grafana/
└── logs-gateway-dashboard.json
```
