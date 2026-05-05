# User Service Architecture Overview

This document explains what was created in `services/user-service` and what each file is responsible for.

## Purpose of this service

The User Service is the identity and account lifecycle microservice for the DLS project. It provides:

- user registration and login
- JWT token issuance for stateless auth
- account state handling (`ACTIVE`, `SUSPENDED`, `BANNED`)
- basic profile retrieval
- user-domain events published to RabbitMQ
- database schema management with Flyway

---

## Folder structure

- `src/main/java/com/dlsexam/userservice`: Java application code
- `src/main/resources`: application configuration and database migrations
- `src/test/java/com/dlsexam/userservice`: test code
- `api`: OpenAPI and AsyncAPI contracts
- `config`: runtime environment templates
- project root files (`pom.xml`, `Dockerfile`, `docker-compose.yml`, `README.md`)

---

## Build and dependencies

### `pom.xml`

Defines the Spring Boot project and dependencies:

- web/API: `spring-boot-starter-web`
- security: `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`
- data: `spring-boot-starter-data-jpa`, PostgreSQL driver
- validation: `spring-boot-starter-validation`
- messaging: `spring-boot-starter-amqp` (RabbitMQ)
- observability: `spring-boot-starter-actuator`
- migration/versioning: Flyway
- API docs UI: Springdoc OpenAPI
- JWT signing/parsing: JJWT

---

## Application entrypoint

### `src/main/java/com/dlsexam/userservice/UserServiceApplication.java`

Standard Spring Boot main class that starts the service.

---

## Domain and persistence layer

### `src/main/java/com/dlsexam/userservice/domain/AccountStatus.java`

Enum for account state:

- `ACTIVE`
- `SUSPENDED`
- `BANNED`

### `src/main/java/com/dlsexam/userservice/domain/User.java`

JPA entity for the user aggregate:

- `id` (UUID)
- `email`
- `passwordHash`
- `displayName`
- `status`
- `roles` (stored in a separate `user_roles` table)
- `createdAt`, `updatedAt` timestamps

### `src/main/java/com/dlsexam/userservice/repository/UserRepository.java`

Spring Data repository used by the service logic:

- find user by email (case-insensitive)
- check whether email already exists
- standard CRUD from `JpaRepository`

---

## DTOs

### `src/main/java/com/dlsexam/userservice/dto/AuthDtos.java`

Request/response records for API endpoints:

- `RegisterRequest`
- `LoginRequest`
- `AuthResponse`
- `UserProfileResponse`
- `SuspendUserRequest`
- `PasswordResetRequest`

Also includes validation constraints (email format, password length, non-blank fields).

---

## Service/business logic

### `src/main/java/com/dlsexam/userservice/service/UserService.java`

Contains core workflows:

- register user, hash password, assign default role, issue JWT
- login user and issue JWT
- return current profile (`/me`)
- suspend account
- delete user
- placeholder for password reset request flow

Also publishes domain events for registration/suspension/deletion.

---

## API controllers

### `src/main/java/com/dlsexam/userservice/controller/AuthController.java`

Authentication endpoints:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/password-reset/request`

### `src/main/java/com/dlsexam/userservice/controller/AdminUserController.java`

Admin account-lifecycle endpoints:

- `POST /api/v1/users/{id}/suspend`
- `DELETE /api/v1/users/{id}`

These are protected with role checks (`ADMIN`).

### `src/main/java/com/dlsexam/userservice/controller/OAuthController.java`

Simple OAuth status endpoint:

- `GET /api/v1/oauth/google/status`

This is currently a configuration-status placeholder, not a full Google login callback flow.

---

## Security layer

### `src/main/java/com/dlsexam/userservice/config/SecurityConfig.java`

Central security configuration:

- stateless sessions (`SessionCreationPolicy.STATELESS`)
- disables CSRF for token-based API usage
- allows public access to:
  - health and docs endpoints
  - auth POST endpoints
  - OAuth status endpoint
- requires authentication for everything else
- registers DAO authentication provider with:
  - `UserDetailsService`
  - `BCryptPasswordEncoder`
- adds JWT auth filter before username/password filter

The selected snippet:

```java
AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
}
```

means:

- authentication uses your database-backed user lookup (`UserDetailsService`)
- passwords are verified using BCrypt hashing
- login checks compare plaintext login input against stored BCrypt hashes

### `src/main/java/com/dlsexam/userservice/security/UserDetailsServiceImpl.java`

Loads a user by email from the repository and maps it into a Spring Security principal.

### `src/main/java/com/dlsexam/userservice/security/UserPrincipal.java`

Custom authenticated user model implementing `UserDetails`:

- exposes user id/email
- maps string roles to authorities (`ROLE_*`)
- reflects account status in lock/enable checks

### `src/main/java/com/dlsexam/userservice/security/JwtService.java`

JWT utilities:

- generate signed token
- parse and validate claims
- configure expiration from properties

### `src/main/java/com/dlsexam/userservice/security/JwtAuthenticationFilter.java`

Request filter that:

- reads `Authorization: Bearer <token>`
- validates/parses token
- resolves user via `UserDetailsService`
- sets `SecurityContext` so protected endpoints can be accessed

---

## Messaging layer

### `src/main/java/com/dlsexam/userservice/config/RabbitConfig.java`

AMQP/RabbitMQ config:

- declares topic exchange from `app.messaging.exchange`
- configures JSON message conversion via Jackson

### `src/main/java/com/dlsexam/userservice/messaging/UserEventPublisher.java`

Publishes domain events through RabbitMQ:

- `user.registered`
- `user.suspended`
- `user.deleted`

Event payloads include user identifiers and timestamps.

---

## Configuration and migrations

### `src/main/resources/application.yml`

Runtime configuration for:

- server port
- datasource
- JPA behavior
- Flyway migration path
- RabbitMQ connection
- OAuth client placeholders (Google)
- Actuator/Prometheus exposure
- OpenAPI docs path
- app JWT and exchange settings

### `src/main/resources/db/migration/V1__create_users_schema.sql`

Initial schema migration:

- creates `users` table
- creates `user_roles` table with FK and cascade delete
- adds lower-email index for case-insensitive lookup performance

---

## API contract files

### `api/openapi.yaml`

OpenAPI contract stub listing key REST endpoints and summaries.  
It currently serves as a starter specification and can be expanded with:

- request/response schemas
- examples
- auth security schemes
- error response models

### `api/asyncapi.yaml`

AsyncAPI contract stub listing published channels/events:

- `user.registered`
- `user.suspended`
- `user.deleted`

It can be expanded with:

- message payload schemas
- broker bindings
- producer metadata and examples

---

## Containerization and local runtime

### `Dockerfile`

Multi-stage build:

1. build JAR with Maven + JDK 21
2. run JAR on lightweight JRE 21 image

### `.dockerignore`

Prevents unnecessary files from entering Docker build context (`target`, IDE files, logs).

### `docker-compose.yml`

Local stack for only this service:

- `user-service` (this application)
- `user-service-db` (PostgreSQL)
- `user-service-rabbitmq` (RabbitMQ + management UI)

Includes health checks and dependency ordering.

---

## Operational docs

### `README.md`

Quick usage guide:

- run locally
- build/run Docker image
- run full local stack with Docker Compose

### `src/test/java/com/dlsexam/userservice/UserServiceApplicationTests.java`

Basic Spring Boot context-load test to verify application wiring starts.

---

## Notes on current maturity

This service is a strong scaffold aligned with the architecture docs.  
It is intentionally minimal in a few areas and can be enhanced later with:

- full OpenAPI/AsyncAPI schemas and examples
- complete OAuth login callback flow
- richer integration tests (DB + RabbitMQ + endpoint-level tests)
- gateway-level integration once other services are implemented
