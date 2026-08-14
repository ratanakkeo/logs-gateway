# Features Codemap

**Last Updated:** 2026-08-14
**Entry Points:** feature roots under `com.bank.observability.logsgateway`

## Key Modules

| Feature | Purpose | Main types | Depends on |
|---|---|---|---|
| **ingest** | HTTP edge, envelope, async publish | `IngestionController`, `IngestLogRequest`, `LogIngestionService`, `LogEnvelope`, `LogPublisher`, `KafkaLogPublisher`, `IngestMetrics` | masking, routing, config, shared/metrics |
| **masking** | Scrub PAN, strip CVV, mask configured keys | `MaskingPipeline`, `Masker`, `PanMasker` (`@Order(1)`), `CvvMasker` (`@Order(2)`), `ConfigFieldMasker` (`@Order(3)`), `MaskingMetrics` | ingest.domain (`LogEnvelope`), config (`MaskingProperties`) |
| **routing** | Choose Kafka topic | `TopicRouter` | ingest.domain, config (`KafkaTopicProperties`) |
| **appindex** | Consume app topic → OpenSearch | `AppLogIndexer`, `LogIndexWriter`, `OpenSearchBulkIndexer` | ingest.domain, config, shared/metrics |
| **audit** | Consume audit topic → S3 | `AuditArchiveService`, `AuditArchiveWriter`, `S3AuditArchiver` | ingest.domain, config, shared/metrics |
| **config** | Clients, properties, security chain, Kafka error handler | See [packages.md](packages.md) | shared/web (filter beans) |
| **shared** | Filters, Problem Details, Micrometer | `ApiKeyAuthFilter`, `RateLimitFilter`, `RequestLoggingFilter`, `GlobalExceptionHandler`, `LogsGatewayMetrics` | config (`SecurityProperties`) |

Ports (domain interfaces) and adapters:

| Port | Adapter |
|---|---|
| `LogPublisher` | `KafkaLogPublisher` |
| `LogIndexWriter` | `OpenSearchBulkIndexer` |
| `AuditArchiveWriter` | `S3AuditArchiver` |
| `IngestMetrics` / `MaskingMetrics` | `LogsGatewayMetrics` |

## Feature notes

**ingest.** Controller returns `202` after `ingestAsync`. Service normalizes timestamp to UTC (`Instant.now()` if missing), scrubs, routes, publishes, then increments ingest metrics. `KafkaLogPublisher` does not wait on the send future; failures increment `logs_gateway_kafka_send_failures`.

**masking.** Spring injects all `Masker` beans into `MaskingPipeline`. PAN masking walks `message` and nested `data`. CVV keys are dropped from `data` only (message text is not scanned for CVV). Config field names are case-insensitive.

**routing.** `AUDIT` (any case) → `app.kafka.topics.audit`. Otherwise → `app.kafka.topics.app`, falling back to `bank.logs.app`.

**appindex.** Single-record `@KafkaListener` on `${app.kafka.topics.app}`, group `logs-gateway-opensearch`. Buffers until `bulk-size` (500) or `flush-interval-ms` (5000), then bulk-indexes into `app.opensearch.index` (`bank-logs-app`).

**audit.** Batch `@KafkaListener` on `${app.kafka.topics.audit}`, group `logs-gateway-s3-audit`. Buffers to `flush-max-bytes` or `flush-interval-ms`, writes gzip NDJSON. Object key: `{prefix}dt=yyyy-MM-dd/service={serviceName}/{uuid}.jsonl.gz`. Multipart upload when payload ≥ `multipart-threshold-bytes`.

**shared.** `RequestLoggingFilter` (highest precedence) logs method/uri/status/duration and copies `X-Trace-Id` into MDC `traceId`. It is not a substitute for the Kafka partition key (that is always body `traceId`).

## Tests (mirroring, not 1:1)

| Production | Test |
|---|---|
| `IngestionController` | `IngestionControllerTest` (`@WebMvcTest`, filters off) |
| `IngestLogRequest` | `IngestLogRequestTest` |
| `LogIngestionService` | `LogIngestionServiceTest` |
| `KafkaLogPublisher` | `KafkaLogPublisherTest` |
| `MaskingPipeline` + maskers | `MaskingPipelineTest` (covers PAN, CVV, config fields, chain) |
| `TopicRouter` | `TopicRouterTest` |
| `AppLogIndexer` | `AppLogIndexerTest` |
| `OpenSearchBulkIndexer` | `OpenSearchBulkIndexerTest` |
| `AuditArchiveService` | `AuditArchiveServiceTest` |
| `S3AuditArchiver` | `S3AuditArchiverTest` |
| `ApiKeyAuthFilter` | `ApiKeyAuthFilterTest` |
| `RateLimitFilter` | `RateLimitFilterTest` |
| `LogsGatewayMetrics` | `LogsGatewayMetricsTest` |
| App Kafka ack path | `KafkaAppLogAckIT` (`@Tag("it")`) |

## Related Areas

- [packages.md](packages.md)
- [../current-structure.md](../current-structure.md)
