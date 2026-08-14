# Logs Gateway

Spring Boot 3.4 / Java 21 service that ingests logs over HTTP, masks PAN, routes to Kafka, then indexes application logs in OpenSearch and archives audit logs to S3.

## Local prerequisites

- Java 21
- Maven 3.9+
- Docker (for Kafka and OpenSearch)

```bash
docker compose up -d
mvn spring-boot:run
```

Configure AWS credentials with `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` before sending `AUDIT` logs. Kafka bootstrap is `localhost:9092`. OpenSearch is `http://localhost:9200`.

## Ingest contract

`POST /v1/logs/ingest` requires `traceId`, `serviceName`, and `logType`. Missing fields return `400`. Valid payloads return `202 Accepted` immediately.

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

`logType: AUDIT` is published to `bank.logs.audit` (S3). Every other type goes to `bank.logs.app` (OpenSearch). Kafka partition key is `traceId`.

Producers should treat ingest as fire-and-forget, never log PAN/CVV in application code, and should not write directly to Kafka, OpenSearch, or S3.
