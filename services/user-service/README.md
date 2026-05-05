# User Service

Spring Boot user management microservice implementing:
- registration and login with JWT
- Google OAuth configuration placeholders
- account status lifecycle (active, suspended, banned)
- RabbitMQ domain events (`UserRegistered`, `UserSuspended`, `UserDeleted`)
- Flyway database migrations

## Run locally

```bash
mvn spring-boot:run
```

## Build Docker image

```bash
docker build -t dls/user-service:local .
docker run --rm -p 8081:8081 dls/user-service:local
```

## Run with Docker Compose

```bash
docker compose up --build
```

This starts:
- `user-service` on `http://localhost:8081`
- PostgreSQL on `localhost:5432`
- RabbitMQ on `localhost:5672` and management UI on `http://localhost:15672` (`guest`/`guest`)
