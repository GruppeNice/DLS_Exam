# Shared Docker and Compose

This folder contains project-wide container definitions for the DLS platform.

## Files

| File | Purpose |
|------|---------|
| `Dockerfile.spring-service` | Multi-stage build for Java/Spring Boot services |
| `Dockerfile.python-service` | Build for Python/FastAPI services |
| `docker-compose.yml` | Full local platform (one RabbitMQ, per-service PostgreSQL) |
| `.env.example` | Shared environment defaults |

## Run the full platform

From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

## Run a subset

```bash
docker compose -f infra/docker/docker-compose.yml up --build user-service billing-service rabbitmq
```

## Build a single Java service image

```bash
docker build -f infra/docker/Dockerfile.spring-service \
  --build-arg SERVICE_MODULE=services/billing-service \
  --build-arg SERVICE_ARTIFACT=billing-service \
  --build-arg SERVICE_PORT=8084 \
  .
```

## Services and ports

| Service | Port |
|---------|------|
| user-service | 8081 |
| catalog-service | 8082 |
| streaming-service | 8083 |
| billing-service | 8084 |
| recommendation-service | 8090 |
| rabbitmq (management UI) | 15672 |
| PostgreSQL (user/billing/streaming/rec/catalog) | 5432 / 5433 / 5434 / 5435 / 5436 |

Per-service `docker-compose.yml` files include this shared compose file for convenience.
