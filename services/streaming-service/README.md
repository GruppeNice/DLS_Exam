# Streaming Service

Spring Boot streaming/playback microservice implementing:

- playback session start, stop, and resume
- watch progress tracking
- active subscription validation via Billing Service
- simulated DRM validation hook
- idempotent session start operations
- RabbitMQ domain events (`PlaybackStarted`, `PlaybackStopped`, `PlaybackProgressUpdated`)
- Flyway database migrations

## Run locally

```bash
# from repository root
mvn spring-boot:run -pl services/streaming-service
```

Requires Billing Service for subscription checks when testing playback.

## Docker

From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build streaming-service streaming-service-db billing-service rabbitmq
```

Or from this folder:

```bash
docker compose up --build streaming-service streaming-service-db billing-service rabbitmq
```

This starts:

- `streaming-service` on `http://localhost:8083`
- MySQL on `localhost:3308`
- Shared RabbitMQ on `localhost:5672`
- `billing-service` on `http://billing-service:8084` inside the compose network

## API overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/playback/start` | JWT + `Idempotency-Key` | Start playback |
| GET | `/api/v1/playback/sessions/me` | JWT | List user sessions |
| GET | `/api/v1/playback/sessions/{id}` | JWT | Get session |
| POST | `/api/v1/playback/sessions/{id}/stop` | JWT | Stop session |
| POST | `/api/v1/playback/sessions/{id}/resume` | JWT | Resume session |
| PUT | `/api/v1/playback/sessions/{id}/progress` | JWT | Update watch progress |

JWT tokens are issued by the User Service and validated using the shared secret.
