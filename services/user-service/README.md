# User Service

Spring Boot user management microservice implementing:
- registration and login with JWT
- Google OAuth configuration placeholders
- account status lifecycle (active, suspended, banned)
- RabbitMQ domain events (`UserRegistered`, `UserSuspended`, `UserDeleted`)
- Flyway database migrations

## Run locally

```bash
# from repository root
mvn spring-boot:run -pl services/user-service
```

## Docker

Shared Docker files live in `infra/docker/`. From the **repository root**:

```bash
# Full platform
docker compose -f infra/docker/docker-compose.yml up --build

# This service only (plus its DB and shared RabbitMQ)
docker compose -f infra/docker/docker-compose.yml up --build user-service user-service-db rabbitmq
```

Or from this folder (includes the shared compose file):

```bash
docker compose up --build user-service user-service-db rabbitmq
```

Endpoints when running:
- `user-service` on `http://localhost:8081`
- MySQL on `localhost:3306`
- RabbitMQ on `localhost:5672` and management UI on `http://localhost:15672` (`guest`/`guest`)
