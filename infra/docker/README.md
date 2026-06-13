# Shared Docker and Compose

This folder contains project-wide container definitions for the DLS platform.

## Files

| File | Purpose |
|------|---------|
| `Dockerfile.spring-service` | Multi-stage build for Java/Spring Boot services |
| `Dockerfile.python-service` | Build for Python/FastAPI services |
| `docker-compose.yml` | Full local platform (one RabbitMQ, MySQL per service) |
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

| Service | HTTP Port |
|---------|-----------|
| user-service | 8081 |
| catalog-service | 8082 |
| streaming-service | 8083 |
| billing-service | 8084 |
| review-rating-service | 8085 |
| engagement-service | 8086 |
| recommendation-service | 8090 |
| **frontend** (test UI) | **3000** |
| rabbitmq (management UI) | 15672 |
| mailhog (web UI) | 8025 |

| MySQL database | Host port |
|----------------|-----------|
| user_db | 3306 |
| billing_db | 3307 |
| streaming_db | 3308 |
| recommendation_db | 3309 |
| catalog_db | 3310 |
| review_db | 3311 |
| engagement_db | 3312 |

Per-service `docker-compose.yml` files include this shared compose file for convenience.
