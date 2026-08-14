# Packages Codemap

**Last Updated:** 2026-08-14
**Entry Points:** `com.bank.observability.logsgateway`

Root: `src/main/java/com/bank/observability/logsgateway/`. Full file tree: [../current-structure.md](../current-structure.md).

There is **no** `HttpClientConfig`. OpenSearch uses `RestClient` inside `OpenSearchClientConfig`.

## Package map

```
logsgateway
├── LogsGatewayApplication          @SpringBootApplication + @ConfigurationPropertiesScan
├── ingest
│   ├── api                         HTTP only
│   ├── domain                      envelope, use case, LogPublisher port
│   └── infrastructure              KafkaLogPublisher
├── masking/domain                  Masker chain (no api/infrastructure)
├── routing/domain                  TopicRouter
├── appindex
│   ├── domain                      AppLogIndexer + LogIndexWriter port
│   └── infrastructure              OpenSearchBulkIndexer
├── audit
│   ├── domain                      AuditArchiveService + AuditArchiveWriter port
│   └── infrastructure              S3AuditArchiver
├── config                          Spring beans + @ConfigurationProperties
└── shared
    ├── web                         servlet filters
    ├── error                       GlobalExceptionHandler
    └── metrics                     LogsGatewayMetrics
```

## Dependency rules (live)

- `domain/` does not import `api/` or `infrastructure/`.
- `api/` may import `domain/` (controller → service, request → envelope).
- `infrastructure/` implements domain ports and may import `config/` and `shared/metrics`.
- `domain/` currently imports `config/` for typed properties (`TopicRouter` → `KafkaTopicProperties`, `ConfigFieldMasker` → `MaskingProperties`). Do not treat that as a license to import Kafka/S3/OpenSearch clients into domain.
- `config/SecurityConfig` constructs `shared/web` filters. `shared/web` reads `SecurityProperties`.

## config/ inventory

| Class | Role |
|---|---|
| `AsyncConfig` | `@EnableAsync` / `@EnableScheduling`; bean `virtualThreadExecutor` |
| `AwsS3Properties` | `app.aws` (region, keys, nested `s3`) |
| `DownstreamGuard` | Retry + circuit breaker runner |
| `KafkaConsumerConfig` | `DefaultErrorHandler` → `{topic}.DLT` |
| `KafkaProducerConfig` | `acks=all`, idempotence, `KafkaTemplate<String, LogEnvelope>` |
| `KafkaTopicProperties` | `app.kafka.topics.app` / `.audit` |
| `MaskingProperties` | `app.masking.fields` |
| `OpenSearchClientConfig` | Java API client over RestClient |
| `OpenSearchProperties` | `app.opensearch` endpoint, index, bulk, flush |
| `S3ClientConfig` | AWS SDK v2 client; path-style when `app.aws.s3.endpoint` set |
| `SecurityConfig` | filter chain; actuator + `/v1/logs/**` permitAll (API key is filter-level) |
| `SecurityProperties` | `app.security.api-keys` and rate-limit |

## shared/ inventory

| Class | Role |
|---|---|
| `ApiKeyAuthFilter` | `/v1/logs/**` + `X-API-Key` |
| `RateLimitFilter` | `/v1/logs/**` Bucket4j per key |
| `RequestLoggingFilter` | access log + MDC `traceId` from `X-Trace-Id` |
| `GlobalExceptionHandler` | validation → 400 Problem Details; other → 500 |
| `LogsGatewayMetrics` | Micrometer counters/timers/summaries |

## Resources

| File | Role |
|---|---|
| `application.yml` | defaults (topics, OS, S3, masking, security, R4j, actuator) |
| `application-dev.yml` | LocalStack `app.aws.s3.endpoint` |
| `application-prod.yml` | root log level INFO (JSON via logback profile) |
| `application-test.yml` | `test-key`, higher rate limit |
| `logback-spring.xml` | JSON `LogstashEncoder` on `prod`; plain pattern otherwise |

## Related Areas

- [features.md](features.md)
- [../current-structure.md](../current-structure.md)
