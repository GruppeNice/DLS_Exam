# Observability Stack

Local metrics, logs, and distributed tracing for the DLS platform.

| Tool | URL | Purpose |
|------|-----|---------|
| **Grafana** | http://localhost:3001 | Dashboards (login: `admin` / `admin`) |
| **Prometheus** | http://localhost:9090 | Metrics scraping |
| **Loki** | http://localhost:3100 | Log aggregation (query via Grafana) |
| **Zipkin** | http://localhost:9411 | Distributed traces |

Grafana is on port **3001** so it does not clash with the frontend on **3000**.

## Prerequisites

The main platform must be running first so services join the shared `dls-platform` Docker network:

```bash
docker compose -f infra/docker/docker-compose.yml up -d
```

## Start observability

From the repository root:

```bash
docker compose -f infra/observability/docker-compose.yml up -d
```

## Start everything together

```bash
docker compose \
  -f infra/docker/docker-compose.yml \
  -f infra/observability/docker-compose.yml \
  up --build -d
```

## What gets scraped

| Service | Metrics endpoint |
|---------|------------------|
| user-service | `/actuator/prometheus` |
| catalog-service | `/actuator/prometheus` |
| streaming-service | `/actuator/prometheus` |
| billing-service | `/actuator/prometheus` |
| review-rating-service | `/actuator/prometheus` |
| engagement-service | `/actuator/prometheus` |
| recommendation-service | `/metrics` |

Promtail ships container stdout logs to Loki via the Docker socket.

Java services export traces to Zipkin when `ZIPKIN_ENDPOINT` is set (configured in the main compose file).

## Grafana quick start

1. Open http://localhost:3001 and sign in (`admin` / `admin`).
2. **Explore → Prometheus** — query e.g. `http_server_requests_seconds_count`.
3. **Explore → Loki** — query e.g. `{compose_service="streaming-service"}`.
4. **Explore → Zipkin** — search traces after hitting a few API endpoints.

## Files

| Path | Purpose |
|------|---------|
| `prometheus/prometheus.yml` | Scrape targets for all backend services |
| `loki/loki-config.yml` | Loki single-node config |
| `promtail/promtail-config.yml` | Docker log discovery → Loki |
| `grafana/provisioning/` | Auto-configured Prometheus, Loki, Zipkin datasources |
