# User Service

Spring Boot user management microservice implementing:
- registration and login with JWT
- Google OAuth 2.0 (optional — set `GOOGLE_OAUTH_ENABLED=true` and Google credentials)
- account status lifecycle (active, suspended, banned)
- RabbitMQ domain events (`UserRegistered`, `UserSuspended`, `UserDeleted`)
- Flyway database migrations

## Google OAuth (optional)

1. Create OAuth 2.0 credentials in [Google Cloud Console](https://console.cloud.google.com/).
2. Authorized redirect URI: `http://localhost:8081/login/oauth2/code/google`
3. Set environment variables:

```bash
GOOGLE_OAUTH_ENABLED=true
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
GOOGLE_OAUTH_FRONTEND_REDIRECT=http://localhost:3000/oauth/callback
```

4. Restart user-service and open the Stream Console login page — **Continue with Google** appears when enabled.

Check status: `GET /api/v1/oauth/google/status`

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
