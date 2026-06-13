# DLS Exam Monorepo

This repository is scaffolded as a microservices monorepo for a streaming platform.
It is intentionally lightweight and organized for future implementation.

## Repository Structure

```text
.
|-- frontend/
|   |-- src/
|   |-- public/
|   `-- test/
|-- services/
|   |-- user-service/
|   |-- catalog-service/
|   |-- streaming-service/
|   |-- billing-service/
|   |-- review-rating-service/
|   |-- recommendation-service/
|   `-- engagement-service/
|-- infra/
|   |-- docker/
|   |-- k8s/
|   |-- messaging/
|   |-- observability/
|   |-- ci-cd/
|   `-- security/
|-- packages/
|   |-- contracts/
|   |-- shared-types/
|   `-- shared-utils/
|-- docs/
|-- Large Systems - Architecture & Stack.md
|-- SystemArchitectureDesignVer6.drawio.xml
`-- SystemArchitectureDesignVer6.drawio.png
```

## Standard Service Folder Convention

Each service in `services/*` follows one shared convention:

- `src/`: application code.
- `test/`: unit and integration tests.
- `api/`: OpenAPI/GraphQL/AsyncAPI contracts.
- `config/`: runtime and environment-specific configuration templates.

This convention is documented here to avoid repeating identical `README.md` files inside each subfolder.

## Service Ownership Map

- `services/user-service`: identity, authentication, account lifecycle.
- `services/catalog-service`: content metadata and GraphQL queries.
- `services/streaming-service`: playback session lifecycle and tracking.
- `services/billing-service`: subscriptions and payment workflows.
- `services/review-rating-service`: user reviews and ratings.
- `services/recommendation-service`: AI/ML recommendation APIs.
- `services/engagement-service`: asynchronous engagement workflows.

## Infrastructure and Shared Packages

- `infra/docker`: shared Dockerfiles, full-platform `docker-compose.yml`, and `.env.example`
- `infra/k8s`: Kubernetes manifests/charts for local cluster deployment
- `infra/messaging`: event broker topology and messaging setup.
- `infra/observability`: metrics, logs, tracing configuration.
- `infra/ci-cd`: pipeline definitions and quality automation.
- `infra/security`: security policies and templates.
- `packages/contracts`: cross-service API/event contracts.
- `packages/shared-types`: shared DTOs and type models.
- `packages/shared-utils`: reusable helper utilities.

## Build and run

Java services share a parent Maven POM at the repository root. Build everything:

```bash
mvn test
```

Run the full local platform:

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

See [`infra/docker/README.md`](infra/docker/README.md) and [`docs/PROJECT_RUNBOOK.md`](docs/PROJECT_RUNBOOK.md) for details.

## Next Steps

- Implement each service with independent runtime and data ownership.
- Define shared contracts in `packages/contracts`.
- Add local orchestration in `infra/docker` and `infra/k8s`.

## Operations guide

See [`docs/PROJECT_RUNBOOK.md`](docs/PROJECT_RUNBOOK.md) for the full-stack runbook: ports, auth flow, event catalog, local dev workflows, and implementation status across every layer.
