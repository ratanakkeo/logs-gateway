# logs-gateway Codemaps

**Last Updated:** 2026-08-14
**Entry Points:** `LogsGatewayApplication`, `IngestionController`

Spring Boot 3.4 / Java 21 service: ingest logs over HTTP, mask PAN/CVV/configured fields, route to Kafka, index application logs in OpenSearch, archive audit logs to S3.

Base package: `com.bank.observability.logsgateway`. Layout is **package-by-feature**.

## Codemaps

| Map | Use when |
|---|---|
| [architecture.md](architecture.md) | System shape, layers, and non-negotiable contracts |
| [features.md](features.md) | Finding the class for ingest, masking, routing, audit, app index, shared |
| [data-flow.md](data-flow.md) | Tracing a request from HTTP to Kafka to OpenSearch/S3 |
| [packages.md](packages.md) | Package responsibilities, ports, and allowed dependencies |
| [integrations.md](integrations.md) | Kafka, OpenSearch, S3, Docker, Grafana, config keys |

## Related docs

- [../current-structure.md](../current-structure.md) — live `src/main` and `src/test` trees
- [../../README.md](../../README.md) — local runbook
- [../../infra/README.md](../../infra/README.md) — OpenSearch template/ISM and S3 lifecycle apply steps
