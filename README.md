# DLS Exam

Streaming platform monorepo — seven backend services, React frontend, RabbitMQ, MySQL per service.

**More detail:** [`docs/PROJECT_RUNBOOK.md`](docs/PROJECT_RUNBOOK.md)

---

## Setup

**You need:** Docker Desktop (with Compose), Git, and optionally JDK 21 + Maven for local Java dev.

```bash
git clone <repo-url>
cd DLS_Exam
```

**Optional — Google login**

```bash
cp infra/docker/.env.example infra/docker/.env
# Set GOOGLE_OAUTH_ENABLED=true and your Google client id/secret
```

Without OAuth, use the demo account: `demo@dls.local` / `password123`

---

## Start everything

From the repo root:

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

Open **http://localhost:3000** (Stream Console UI).

First boot can take a few minutes while images build and databases migrate.

---

## Observability

Start the platform first, then in another terminal:

```bash
docker compose -f infra/observability/docker-compose.yml up -d
```

| Tool | URL |
|------|-----|
| Grafana | http://localhost:3001 (`admin` / `admin`) |
| Prometheus | http://localhost:9090 |
| Zipkin | http://localhost:9411 |
| RabbitMQ UI | http://localhost:15672 (`guest` / `guest`) |
| MailHog | http://localhost:8025 |

---

## Start individual services

Still from the repo root — add only what you need (include `rabbitmq` and the service’s `*-db` when applicable):

```bash
# Example: user service only
docker compose -f infra/docker/docker-compose.yml up --build user-service user-service-db rabbitmq

# Example: billing + streaming (playback needs billing)
docker compose -f infra/docker/docker-compose.yml up --build billing-service billing-service-db streaming-service streaming-service-db rabbitmq
```

**Compose service names:** `user-service`, `billing-service`, `streaming-service`, `catalog-service`, `review-rating-service`, `engagement-service`, `recommendation-service`, `frontend`, `rabbitmq`, `mailhog`, plus `*-service-db` for each backend.

**Run one service with Maven** (needs local MySQL/RabbitMQ or the DB container running):

```bash
mvn spring-boot:run -pl services/user-service
```

---

## Ports

| Service | Port |
|---------|------|
| Frontend | 3000 |
| user-service | 8081 |
| catalog-service | 8082 |
| streaming-service | 8083 |
| billing-service | 8084 |
| review-rating-service | 8085 |
| engagement-service | 8086 |
| recommendation-service | 8090 |

---

## API endpoints

Base URLs are `http://localhost:<port>`. Most write operations need `Authorization: Bearer <jwt>` from login.

### user-service (8081)

| Method | Path |
|--------|------|
| POST | `/api/v1/auth/register` |
| POST | `/api/v1/auth/login` |
| GET | `/api/v1/auth/me` |
| GET | `/api/v1/oauth/google/status` |
| GET | `/oauth2/authorization/google` (OAuth, when enabled) |
| GET | `/actuator/health` |
| GET | `/swagger-ui.html` |

### catalog-service (8082)

| Method | Path |
|--------|------|
| POST | `/graphql` |
| GET | `/graphiql` |
| GET | `/actuator/health` |

### streaming-service (8083)

| Method | Path |
|--------|------|
| POST | `/api/v1/playback/start` |
| POST | `/api/v1/playback/sessions/{id}/stop` |
| POST | `/api/v1/playback/sessions/{id}/resume` |
| PUT | `/api/v1/playback/sessions/{id}/progress` |
| GET | `/api/v1/playback/sessions/me` |
| GET | `/actuator/health` |
| GET | `/swagger-ui.html` |

### billing-service (8084)

| Method | Path |
|--------|------|
| GET | `/api/v1/plans` |
| POST | `/api/v1/subscriptions` |
| GET | `/api/v1/subscriptions/me` |
| GET | `/api/v1/subscriptions/active/{userId}` |
| POST | `/api/v1/subscriptions/{id}/cancel` |
| POST | `/api/v1/payments` |
| GET | `/api/v1/invoices` |
| GET | `/actuator/health` |
| GET | `/swagger-ui.html` |

### review-rating-service (8085)

| Method | Path |
|--------|------|
| * | `/ratings`, `/reviews` (CRUD — see Swagger) |
| GET | `/review-votes/{reviewId}` |
| POST | `/review-votes/add` |
| GET | `/actuator/health` |
| GET | `/swagger-ui.html` |

### engagement-service (8086)

| Method | Path |
|--------|------|
| POST | `/api/notifications` |
| GET | `/api/notifications/{id}` |
| GET | `/actuator/health` |

### recommendation-service (8090)

| Method | Path |
|--------|------|
| GET | `/api/v1/health` |
| GET | `/api/v1/recommendations/me` |
| GET | `/api/v1/recommendations/trending` |
| POST | `/api/v1/recommendations/retrain` |
| POST | `/api/v1/recommendations/interactions` |

OpenAPI: `services/recommendation-service/api/openapi.yaml`

---

## Tests & Kubernetes

```bash
mvn test                    # all Java services
```

Local Kubernetes: see [`infra/k8s/README.md`](infra/k8s/README.md)
