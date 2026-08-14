# Data Flow Codemap

**Last Updated:** 2026-08-14
**Entry Points:** `IngestionController.ingest`, `AppLogIndexer.index`, `AuditArchiveService.archive`

## Happy path

```
1. Client POST /v1/logs/ingest
      Header: X-API-Key, Content-Type: application/json
      Body:   IngestLogRequest  →  LogEnvelope

2. Filters ( /v1/logs/** only )
      ApiKeyAuthFilter   → 401 if key missing/unknown
      RateLimitFilter    → 429 if bucket empty

3. Validation
      @Valid @NotBlank on traceId, serviceName, logType
      else 400 Problem Details (GlobalExceptionHandler)

4. Controller
      ingestAsync(payload.toEnvelope())
      return 202 Accepted          // does not wait for Kafka

5. LogIngestionService (virtual thread)
      timestamp UTC (now if null)
      MaskingPipeline.scrub
      TopicRouter.resolve
      LogPublisher.publish(topic, traceId, envelope)
      IngestMetrics.recordIngest(logType)

6a. App branch (logType != AUDIT)
      topic bank.logs.app
      AppLogIndexer  → LogIndexWriter.index
      OpenSearchBulkIndexer buffer → bulk to alias bank-logs-app
      acknowledgment.acknowledge()

6b. Audit branch (logType == AUDIT)
      topic bank.logs.audit
      AuditArchiveService (batch) → AuditArchiveWriter.archive
      S3AuditArchiver buffer → gzip NDJSON → S3
      acknowledgment.acknowledge()
```

## Failure paths

| Stage | What happens |
|---|---|
| Bad JSON / missing required fields | `400` `Invalid log payload`; nothing published |
| Wrong/missing API key | `401`; controller never runs |
| Rate limit exceeded | `429` + `Retry-After` |
| Kafka send `whenComplete` error | Counter `logs_gateway_kafka_send_failures`; HTTP already returned 202 |
| Kafka / OpenSearch / S3 throws inside `DownstreamGuard` | Retry then circuit breaker (`kafka` / `opensearch` / `s3`) |
| Listener processing exhausts backoff | Record published to `{topic}.DLT`, same partition |
| OpenSearch bulk `errors() == true` | Treated as failure (no ack until handler recovers or DLT) |
| S3 multipart fails mid-upload | Abort multipart, exception propagates (no ack) |

HTTP ingest is fire-and-forget: a 202 does not mean the envelope is in OpenSearch or S3 yet.

## Envelope

`LogEnvelope` is the internal (and Kafka JSON) record:

`timestamp`, `serviceName`, `traceId`, `logLevel`, `logType`, `message`, `data`

Consumers deserialize with `spring.json.value.default.type` = `LogEnvelope` and `spring.json.use.type.headers=false`. Trusted package: `com.bank.observability.logsgateway.ingest.domain`.

## Partitioning and identity

- Kafka key = `traceId` (set by `KafkaLogPublisher.publish`).
- OpenSearch document id is not set; OpenSearch assigns ids. Search/correlation is on mapped fields `traceId` / `serviceName`.
- S3 objects are partitioned by UTC date and first envelope `serviceName` in the flushed batch.

## Related Areas

- [architecture.md](architecture.md)
- [integrations.md](integrations.md)
