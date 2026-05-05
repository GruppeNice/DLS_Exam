# User Service Report Draft (Current State)

This file is a writing helper for the report sections you listed.  
It only describes what is already implemented or clearly prepared in the current `user-service`.

---

## 3. Environments Description

### 3.1 Development environment with docker-compose including the architecture diagram

The User Service is runnable as an isolated local microservice stack using Docker Compose (`services/user-service/docker-compose.yml`).  
The stack currently contains:

- `user-service` (Spring Boot application)
- `user-service-db` (PostgreSQL 16)
- `user-service-rabbitmq` (RabbitMQ with management UI)

The compose setup includes health checks and startup dependencies so the application waits for database and broker readiness before startup.  
This supports realistic local development for authentication, persistence, and event publishing workflows.

Suggested diagram description for this subsection:

1. Client request enters `user-service` (port `8081`)
2. Service persists user/account data to PostgreSQL
3. Service publishes user-domain events to RabbitMQ exchange
4. Monitoring endpoints are exposed via Actuator/Prometheus-compatible path

### 3.2 Local Kubernetes deployment simulating production-like environment including architecture diagram

A full Kubernetes deployment for this service has not yet been implemented in manifests, but the service is prepared for container-first deployment:

- stateless application container (`Dockerfile`)
- environment-variable based configuration (`application.yml`, `.env.example`)
- external dependencies (PostgreSQL and RabbitMQ) already separated as services

Planned K8s shape (based on current service design):

- `Deployment` for `user-service`
- `Service` for internal cluster routing
- `ConfigMap` for non-sensitive app config
- `Secret` for credentials and JWT secret
- optional `Ingress` for external access

### 3.2.1 Description of used technologies

Technologies already used in the current User Service:

- Java 21 + Spring Boot 3.5
- Spring Security + JWT (JJWT)
- Spring Data JPA + PostgreSQL
- Flyway for DB migrations
- Spring AMQP + RabbitMQ for event publishing
- Spring Actuator for operational endpoints
- Springdoc OpenAPI
- AsyncAPI contract stub
- Docker + Docker Compose

### 3.2.2 CI/CD pipeline description and diagram

A complete pipeline file is not yet included specifically for `user-service`, but the service structure supports standard CI/CD stages:

1. compile/build (Maven)
2. run tests
3. run static analysis/security scans
4. build Docker image
5. publish image to registry
6. deploy/update environment

This stage model is compatible with the project-level CI/CD architecture in the main documentation.

### 3.2.3 Monitoring and logging of the deployed system

The service exposes observability-ready endpoints through Spring Actuator.  
In `application.yml`, endpoint exposure includes `health`, `info`, `metrics`, and `prometheus`, enabling integration with Prometheus/Grafana stacks.

This aligns with the architecture decision to use centralized monitoring and distributed service observability.

### 3.2.4 Autoscaling configuration

Autoscaling is not yet configured specifically for the User Service.  
However, the application is stateless and containerized, which makes it suitable for horizontal pod autoscaling in Kubernetes.

Current design implications:

- app tier can scale horizontally
- DB tier requires different scaling strategy (capacity tuning, replicas, managed DB options)
- messaging throughput can be tuned via broker configuration and consumer behavior in downstream services

### 3.3 Cloud deployment plan

No cloud deployment is currently executed (not required by assignment), but this service is designed for straightforward cloud adoption:

- containerized deployment
- externalized config and secrets
- independent data ownership
- event-driven integration boundary through RabbitMQ

Reasonable cloud plan for this service:

- deploy app container to managed Kubernetes/container platform
- use managed PostgreSQL for durability and backup strategy
- use managed message broker or cloud queue equivalent
- use secret manager for JWT secret and DB credentials
- use rolling updates with health-based rollout checks

---

## 4. Testing

### 4.1 Testing strategy and scope

Current testing scope is minimal but defined around service bootstrap and architecture readiness:

- application context load test exists
- API and business logic are structured for further unit and integration tests
- security, eventing, and persistence boundaries are clearly separated for testability

### 4.2 Unit testing

Unit tests should target:

- `UserService` business rules (register/login/suspend/delete behavior)
- `JwtService` token generation and claim parsing
- DTO validation edge cases
- role and account-status behavior in security model

At current state, unit test coverage is still a planned extension.

### 4.3 Integration testing

The architecture supports integration tests against real infrastructure:

- PostgreSQL + Flyway migration verification
- repository interaction tests
- authentication endpoint flow tests
- RabbitMQ publication checks for user events

Current repository has a baseline test scaffold; expanded integration tests remain to be added.

### 4.4 System-level cooperation testing

The service already emits integration events (`user.registered`, `user.suspended`, `user.deleted`).  
This enables future cooperation tests with consuming services (recommendation, engagement, analytics).

At current stage, system cooperation tests are not yet implemented end-to-end.

### 4.5 Security testing focusing on authentication and authorization

Security implementation already supports targeted testing:

- password hashing with BCrypt
- JWT-based stateless authentication
- protected routes requiring valid bearer token
- admin-only routes enforced with role checks (`hasRole('ADMIN')`)
- account-status restrictions (`SUSPENDED`, `BANNED`)

These are suitable for automated security-focused API tests.

### 4.6 Static analysis and static testing

Static analysis tooling is not yet fully configured per service, but intended checks include:

- Java static analyzers
- dependency vulnerability scanning
- API contract linting

This section can be expanded once concrete tools are added to CI.

### 4.7 Test execution in CI/CD

Current service structure is ready for CI test execution flow:

1. compile
2. run unit tests
3. run integration tests
4. fail pipeline on security/static analysis issues

Detailed CI wiring remains a next implementation step.

### 4.8 Limitations, risks, and trade-offs

Current limitations:

- OpenAPI and AsyncAPI files are starter-level stubs
- OAuth is currently status/config placeholder, not full external login callback flow
- limited automated test coverage at this stage

Trade-off chosen:

- prioritize architecture-aligned service scaffold first
- defer deep contract/testing maturity to next iteration

---

## 5. Project Management and Team Collaboration

### 5.1 Introduction to project management and collaboration

The User Service was implemented within a monorepo structure with clear service boundaries.  
This supports parallel development and ownership per microservice.

### 5.2 Methods used during the project

Current work reflects iterative delivery:

- scaffold service structure
- implement security, persistence, and eventing core
- add runtime environments (Docker + Compose)
- document architecture and contracts for future extension

### 5.3 Versioning strategies for source code, databases, and APIs

Current strategy already visible in codebase:

- source code: Git-based versioning in monorepo
- database: Flyway SQL migration files (`V1__...`)
- API versioning: `/api/v1/...` endpoint prefix
- contracts: separate OpenAPI and AsyncAPI files in `api/`

### 5.4 Documentation strategy

Current User Service documentation artifacts:

- `README.md` (run/build usage)
- `ARCHITECTURE_OVERVIEW.md` (technical breakdown)
- `openapi.yaml` and `asyncapi.yaml` (contract stubs)
- configuration template (`config/.env.example`)

This layered documentation supports both developer onboarding and report writing.

---

## 6. Discussion

### 6.1 Advantages and challenges of distributed systems

Advantages shown by current User Service:

- clear bounded context (identity/account lifecycle)
- independent deployment and scaling potential
- isolated data ownership
- asynchronous integration via messaging

Challenges already visible:

- more moving parts in local setup (DB + broker + app)
- higher testing complexity across service boundaries
- contract discipline required for event and API evolution

### 6.2 Pros and cons of used patterns (e.g., CQRS, event-driven)

Used pattern strengths:

- event-driven publishing decouples producers and consumers
- stateless JWT reduces per-request auth state dependency

Current constraints:

- full CQRS split is not explicitly implemented in this service
- event contracts are currently minimal and need richer schemas

### 6.3 Scalability/autoscaling (app, messaging, database)

Current architecture supports:

- horizontal scaling of stateless app tier
- asynchronous workload decoupling through broker

Scalability bottlenecks and considerations:

- database throughput and connection limits
- broker resource tuning under high event volume
- consumer lag management in downstream services

### 6.4 Possible improvements

Most relevant next improvements:

- full OAuth login/callback flow implementation
- richer OpenAPI/AsyncAPI schemas and examples
- stronger automated testing coverage
- hardened secret management and token lifecycle features (refresh/revocation)

---

## 7. Reflection

At current stage, key learning points from User Service implementation include:

- security setup is conceptually simple but integration-heavy (filters, providers, roles, tokens)
- event-driven design requires explicit contract and naming discipline from the start
- database schema versioning with Flyway gives safer iteration compared to ad-hoc schema edits
- starting with a strong scaffold reduces later rework across architecture, deployment, and docs

---

## 8. Conclusion

The current User Service implementation provides a solid architecture-aligned baseline: secure REST API, independent persistence, event emission, and containerized local runtime.  
While some areas are intentionally minimal (contracts, OAuth depth, tests), the foundation is in place for iterative hardening and full project integration.

---

## 9. References (what to include)

For report quality, cite official sources for:

- Spring Boot and Spring Security
- JWT specification and JJWT docs
- RabbitMQ and AMQP concepts
- Flyway migration/versioning
- Docker and Docker Compose
- Kubernetes deployment practices
- Prometheus and Actuator observability

Use one consistent citation format (APA/IEEE/Harvard).

---

## 10. Appendix

Possible appendix items from current state:

- endpoint list from `api/openapi.yaml`
- event channel list from `api/asyncapi.yaml`
- key environment variables from `config/.env.example`
- compose service topology from `docker-compose.yml`
