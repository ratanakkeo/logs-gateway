# Infra apply steps

These files are documentation plus apply-able JSON. They are not applied automatically.

The application still writes application logs to the `bank-logs-app` alias (see `app.opensearch.index`). Create a write index that uses that alias after the template is in place.

## OpenSearch index template (daily rollover alias)

```bash
curl -sS -X PUT "http://localhost:9200/_index_template/bank-logs-app" \
  -H 'Content-Type: application/json' \
  -d @infra/opensearch/index-template.json

curl -sS -X PUT "http://localhost:9200/bank-logs-app-000001" \
  -H 'Content-Type: application/json' \
  -d '{"aliases":{"bank-logs-app":{"is_write_index":true}}}'
```

## ISM policy (delete after 30 days)

```bash
curl -sS -X PUT "http://localhost:9200/_plugins/_ism/policies/bank-logs-app-retention" \
  -H 'Content-Type: application/json' \
  -d @infra/opensearch/ism-policy.json
```

## S3 lifecycle (Glacier after 90 days on `audit/`)

LocalStack does not enforce Glacier the same way AWS does. Apply this against a real bucket when promoting:

```bash
aws s3api put-bucket-lifecycle-configuration \
  --bucket bank-audit-logs-worm \
  --lifecycle-configuration file://infra/s3/lifecycle.json
```
