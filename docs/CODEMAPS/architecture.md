# Architecture Codemap

**Last Updated:** 2026-08-14
**Entry Points:** `src/main/java/com/bank/observability/logsgateway/LogsGatewayApplication.java`

## Architecture

```
HTTP producers
      |
      |  POST /v1/logs/ingest   X-API-Key
      v
 IngestionController  ----202 Accepted---->  caller (fire-and-forget)
      |
      v  @Async virtualThreadExecutor
 LogIngestionService
      |-- MaskingPipeline (PAN -> CVV strip -> config fields)
      |-- TopicRouter     (AUDIT vs everything else)
      v
 KafkaLogPublisher    key = traceId, acks=all, idempotent
      |
      +------------------+------------------+
      v                                     v
 bank.logs.app                         bank.logs.audit
      |                                     |
 AppLogIndexer                         AuditArchiveService
 group logs-gateway-opensearch         group logs-gateway-s3-audit
 manual ack after index()              batch + manual ack after archive()
      |                                     |
 OpenSearchBulkIndexer                 S3AuditArchiver
 alias bank-logs-app                   s3://bank-audit-logs-worm/audit/...
      |                                     |
 poison -> bank.logs.app.DLT           poison -> bank.logs.audit.DLT
```

Feature packages own `api/` (HTTP), `domain/` (use cases + ports), and `infrastructure/` (Kafka/OpenSearch/S3 adapters). `domain/` must not import `api/` or `infrastructure/`. Cross-cutting beans live in `config/` and `shared/`.

There is no application database. State is Kafka offsets plus downstream stores.

## Ingest contract (do not break)

| Rule | Behavior |
|---|---|
| Endpoint | `POST /v1/logs/ingest` |
| Required body | `traceId`, `serviceName`, `logType` (`@NotBlank`) |
| Missing fields | `400` Problem Details (`Invalid log payload`) |
| Valid payload | `202 Accepted` immediately; work continues on a virtual thread |
| `logType` AUDIT | Kafka `bank.logs.audit` → S3 |
| Any other `logType` | Kafka `bank.logs.app` → OpenSearch |
| Partition key | `traceId` |

Optional body fields on the live record: `timestamp`, `logLevel`, `message`, `data`. Unknown JSON properties are ignored.

## Kafka / masking rules

- Producer: `acks=all`, `enable.idempotence=true`, `max.in.flight.requests.per.connection=5`.
- Consumers: `ack-mode=MANUAL`; acknowledge only after OpenSearch index or S3 archive succeeds.
- Poison: `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` → `{originalTopic}.DLT` (same partition).
- PAN: Luhn-aware 13–19 digit candidates; retain at most first-6 + last-4.
- CVV: keys `cvv`, `cvc`, `csc`, `cid`, `securitycode`, `cardcvv` with 3–4 digit values are stripped (never logged).
- Extra field masking: `app.masking.fields` (default `password`, `pin`, `secret`) → `[MASKED]`.

## Edge protection

Applied only to `/v1/logs/**`:

1. `ApiKeyAuthFilter` — header `X-API-Key` must match `app.security.api-keys` (else `401`).
2. `RateLimitFilter` — Bucket4j per API key (default 100 tokens / 1m; else `429` + `Retry-After`).

`/actuator/health`, `/actuator/info`, `/actuator/prometheus` are permit-all. CSRF, HTTP basic, and form login are off; sessions are stateless.

## Resilience and metrics

`DownstreamGuard` wraps Kafka publish, OpenSearch bulk, and S3 upload with Resilience4j retry + circuit breaker instances named `kafka`, `opensearch`, `s3`.

Custom meters (see Grafana): `logs_gateway_ingest`, `logs_gateway_mask_hits`, `logs_gateway_kafka_send_failures`, `logs_gateway_opensearch_bulk`, `logs_gateway_s3_flush_bytes`.

## Related Areas

- [features.md](features.md) — class-level map
- [data-flow.md](data-flow.md) — request path
- [integrations.md](integrations.md) — brokers, indexes, buckets
