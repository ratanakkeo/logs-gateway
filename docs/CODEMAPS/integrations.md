# Integrations Codemap

**Last Updated:** 2026-08-14
**Entry Points:** `application.yml`, `docker-compose.yml`, `infra/`, `grafana/`

## Kafka

| Item | Value |
|---|---|
| Bootstrap (host) | `localhost:9092` |
| Bootstrap (compose app) | `kafka:19092` |
| App topic | `bank.logs.app` |
| Audit topic | `bank.logs.audit` |
| DLT | `{topic}.DLT` |
| Producer | `acks=all`, `enable.idempotence=true` |
| Listener ack | `MANUAL` |
| App consumer group | `logs-gateway-opensearch` |
| Audit consumer group | `logs-gateway-s3-audit` |
| Image (compose) | `bitnami/kafka:3.9` |
| Image (IT) | `apache/kafka:3.8.0` Testcontainers |

Compose Kafka uses KRaft (`PROCESS_ROLES=controller,broker`) with internal listener `19092` and host listener `9092`.

## OpenSearch

| Item | Value |
|---|---|
| Endpoint (host) | `http://localhost:9200` |
| Write target | alias `bank-logs-app` (`app.opensearch.index`) |
| Bulk | 500 docs or 5000 ms |
| Image | `opensearchproject/opensearch:2.17.1` (security plugin disabled in compose) |
| Dashboards | `http://localhost:5601` |

Apply-able JSON (not auto-applied):

- `infra/opensearch/index-template.json` — pattern `bank-logs-app-*`, rollover alias `bank-logs-app`, mappings for envelope fields
- `infra/opensearch/ism-policy.json` — delete after 30 days

Create the first write index after the template; steps in [infra/README.md](../../infra/README.md).

## S3

| Item | Value |
|---|---|
| Bucket | `bank-audit-logs-worm` |
| Prefix | `audit/` |
| Region | `ap-southeast-1` |
| Object | gzip NDJSON `{prefix}dt=yyyy-MM-dd/service={service}/{uuid}.jsonl.gz` |
| Dev endpoint | `http://localhost:4566` (path-style); compose uses `http://localstack:4566` |
| Prod | do not set `app.aws.s3.endpoint` |
| Lifecycle JSON | `infra/s3/lifecycle.json` — Glacier after 90 days on `audit/` (apply on real AWS; LocalStack does not behave the same) |

Credentials: `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` (compose/dev dummy `test`/`test`).

## HTTP / ops

| Item | Value |
|---|---|
| App port | `8080` |
| Ingest | `POST /v1/logs/ingest` |
| Auth | `X-API-Key` (default `dev-key`, override `LOGS_GATEWAY_API_KEY`) |
| Prometheus | `GET /actuator/prometheus` |
| Grafana | `grafana/logs-gateway-dashboard.json` (datasource var `DS_PROMETHEUS`) |

Dashboard panels: ingest by logType, mask hits, Kafka send failures, OpenSearch bulk duration, S3 flush bytes, HTTP duration, JVM heap.

## Docker

| File | Role |
|---|---|
| `Dockerfile` | Multi-stage: Temurin 21 JDK `./mvnw -DskipTests package` → Temurin 21 JRE `logs-gateway-0.1.0-SNAPSHOT.jar` |
| `docker-compose.yml` | `kafka`, `opensearch`, `dashboards`, `localstack` (S3), `app` |
| `.dockerignore` | `target`, `.git`, `.idea`, `.cursor` |

Compose `app` sets `SPRING_PROFILES_ACTIVE=dev`, Kafka `kafka:19092`, OpenSearch `http://opensearch:9200`, S3 `http://localstack:4566`.

## External libraries (from `pom.xml`)

- Spring Boot **3.4.5**, Java **21**
- spring-kafka, spring-security, actuator, micrometer-prometheus
- OpenSearch Java client **2.12.0** + REST client **2.17.1**
- AWS SDK S3 **2.25.16**
- Bucket4j **8.14.0**, Resilience4j **2.2.0**, Logstash Logback **8.0**
- Test: spring-boot-starter-test, Testcontainers Kafka, Awaitility

## Related Areas

- [architecture.md](architecture.md)
- [data-flow.md](data-flow.md)
- [../../README.md](../../README.md)
