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
mvn spring-boot:run
```

Requires PostgreSQL, RabbitMQ, and Billing Service (for subscription checks).

## Build Docker image

```bash
docker build -t dls/streaming-service:local .
docker run --rm -p 8083:8083 dls/streaming-service:local
```

## Run with Docker Compose

```bash
docker compose up --build
```

This starts:

- `streaming-service` on `http://localhost:8083`
- PostgreSQL on `localhost:5434`
- RabbitMQ on `localhost:5674` and management UI on `http://localhost:15674` (`guest`/`guest`)

When running in Docker, the service calls Billing at `http://host.docker.internal:8084`. Start billing-service separately for subscription validation to work.

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
