# Logs Gateway

Spring Boot 3.4 / Java 21 service that ingests logs over HTTP, masks PAN, routes to Kafka, then indexes application logs in OpenSearch and archives audit logs to S3.

## Local runbook

Prerequisites: Java 21, Maven 3.9+, Docker.

### 1. Start the platform

```bash
docker compose up -d kafka opensearch dashboards localstack
```

Or build and run the app in Compose as well (`:8080`):

```bash
docker compose up -d --build
```

Host Kafka is `localhost:9092`. The app container uses the internal listener `kafka:19092`.

### 2. Create the LocalStack bucket

```bash
aws --endpoint-url=http://localhost:4566 s3 mb s3://bank-audit-logs-worm
```

Dummy LocalStack credentials: `AWS_ACCESS_KEY_ID=test` and `AWS_SECRET_ACCESS_KEY=test`.

### 3. Apply OpenSearch template (optional)

See [infra/README.md](infra/README.md) for the index template, 30-day ISM policy, and S3 Glacier lifecycle JSON.

### 4. Run the app on the host

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile sets `app.aws.s3.endpoint=http://localhost:4566` (path-style). Do not set that endpoint in prod.

### 5. Sample ingest

```bash
curl -sS -X POST http://localhost:8080/v1/logs/ingest \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: dev-key' \
  -d '{
    "timestamp": "2026-08-10T07:00:00Z",
    "serviceName": "payments-api",
    "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
    "logLevel": "INFO",
    "logType": "APPLICATION",
    "message": "payment authorized",
    "data": { "amount": "10.00" }
  }'
```

`POST /v1/logs/ingest` requires `traceId`, `serviceName`, and `logType`. Missing fields return `400`. Valid payloads return `202 Accepted` immediately.

`logType: AUDIT` is published to `bank.logs.audit` (S3). Every other type goes to `bank.logs.app` (OpenSearch). Kafka partition key is `traceId`.

### Where to look

| What | URL |
|---|---|
| OpenSearch Dashboards | http://localhost:5601 |
| OpenSearch API | http://localhost:9200 |
| Prometheus scrape | http://localhost:8080/actuator/prometheus |
| Grafana dashboard JSON | [grafana/logs-gateway-dashboard.json](grafana/logs-gateway-dashboard.json) |
| LocalStack S3 | http://localhost:4566 |

Import the Grafana dashboard and point Prometheus at `/actuator/prometheus`.

## Tests

`mvn verify` runs unit tests only. A Kafka Testcontainers IT (`@Tag("it")`) is excluded from Surefire so the default build stays fast and does not start an OpenSearch container (security plugin / bulk timing made that suite flaky). Run it with Docker available:

```bash
mvn test -Dgroups=it
```

Producers should treat ingest as fire-and-forget, never log PAN/CVV in application code, and should not write directly to Kafka, OpenSearch, or S3.
