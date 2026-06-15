# Engagement Service

Spring Boot microservice implementing:

- asynchronous notification delivery (email via MailHog locally)
- RabbitMQ consumers for domain events (`subscription.activated`, `playback.stopped`, `content.created`, `content.reviewed`)
- internal `notification-queue` for queued outbound messages
- Thymeleaf email templates
- REST endpoints for manual notification triggers and status checks
- optional **job mode** for KEDA ScaledJob (process notifications then exit)

## Run locally (server mode)

Default mode: long-running HTTP service + RabbitMQ listeners.

```bash
# from repository root
cd services/engagement-service
mvn spring-boot:run
```

## Docker

From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build engagement-service engagement-service-db rabbitmq mailhog
```

This starts:

- `engagement-service` on `http://localhost:8086`
- MySQL `engagement_db` on `localhost:3312`
- MailHog UI `http://localhost:8025` (SMTP `1025`)
- Shared RabbitMQ on `localhost:5672`

## API overview

Paths are exposed at the service root (nginx strips `/api/engagement` in Docker).

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/notifications` | Queue a notification (async delivery) |
| GET | `/api/notifications/{id}` | Notification status |

Metrics: `GET /actuator/prometheus`

## Events consumed

| Routing key | Exchange | Action |
|-------------|----------|--------|
| `subscription.activated` | `billing.events` | Welcome email |
| `playback.stopped` | `streaming.events` | Continue-watching style notification |
| `content.created` | `catalog.events` | New content alert |
| `content.reviewed` | `review.events` | Review activity notification |

Queue: `engagement-service.domain-events`

## Job mode (KEDA ScaledJob)

Set `ENGAGEMENT_MODE=job` to run as a short-lived worker: drains up to one message from `notification-queue`, sends it, then exits. Used with KEDA in `infra/k8s/engagement-scaledjob.yaml`.

```bash
ENGAGEMENT_MODE=job mvn spring-boot:run -pl services/engagement-service
```

See `infra/k8s/README.md` for installing KEDA and applying the ScaledJob manifest.

## AsyncAPI

Stub: `api/asyncapi.yaml`. Platform contract: `packages/contracts/asyncapi/dls-platform-events.yaml`.
