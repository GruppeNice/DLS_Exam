# Billing Service (Payment & Billing)

Spring Boot payment and billing microservice implementing:

- subscription plan catalog
- subscription activation and cancellation (saga workflow)
- simulated payment gateway with configurable failure rate
- idempotent payment and activation operations
- invoice generation
- refund handling
- RabbitMQ domain events (`SubscriptionActivated`, `SubscriptionCancelled`, `PaymentSucceeded`, `PaymentFailed`)
- Flyway database migrations

## Run locally

```bash
# from repository root
mvn spring-boot:run -pl services/billing-service
```

## Docker

Shared Docker files live in `infra/docker/`. From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build billing-service billing-service-db rabbitmq
```

Or from this folder:

```bash
docker compose up --build billing-service billing-service-db rabbitmq
```

This starts:

- `billing-service` on `http://localhost:8084`
- PostgreSQL on `localhost:5433`
- Shared RabbitMQ on `localhost:5672` / UI `http://localhost:15672`

## API overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/plans` | Public | List active plans |
| GET | `/api/v1/plans/{id}` | Public | Get plan details |
| POST | `/api/v1/subscriptions` | JWT + `Idempotency-Key` | Activate subscription |
| GET | `/api/v1/subscriptions/me` | JWT | List user subscriptions |
| GET | `/api/v1/subscriptions/active/{userId}` | Public | Check active subscription (for Streaming Service) |
| POST | `/api/v1/subscriptions/{id}/cancel` | JWT | Cancel subscription |
| POST | `/api/v1/payments` | JWT | Process payment |
| POST | `/api/v1/payments/{id}/refund` | JWT | Refund payment |
| GET | `/api/v1/invoices` | JWT | List user invoices |

JWT tokens are issued by the User Service and validated using the shared secret.
