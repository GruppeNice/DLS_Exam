# Large Systems - Architecture & Stack

## 1. Overview
- Problem Statement:
- Scope:
- Goals and Constraints:

## 2. Requirements
- Functional:
- Non-Functional:

## 3. System Architecture
- High-Level Diagram: `SystemArchitectureDesignVer6.drawio.xml` / `SystemArchitectureDesignVer6.drawio.png`
- Service Boundaries:
- Communication Patterns:
- Data Ownership:

## 4. Repository Structure

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
`-- docs/
    |-- roadmap.md
    `-- decisions/
```

## 5. Architecture-to-Folder Mapping

- Frontend layer -> `frontend/`
- API gateway and service logic -> `services/`
- Event broker and async patterns -> `infra/messaging/`
- Data and service contracts -> `packages/contracts/`
- Deployments and runtime platform -> `infra/docker/` and `infra/k8s/`
- CI/CD and quality controls -> `infra/ci-cd/`
- Observability stack -> `infra/observability/`
- Security controls and guidelines -> `infra/security/`
- Cross-service shared code -> `packages/shared-types/` and `packages/shared-utils/`
- Documentation and architecture decisions -> `docs/`

## 6. Technology Stack
- Frontend:
- Backend:
- Messaging/Eventing:
- Databases:
- AI/ML:

## 7. Platform and Operations
- Containerization/Orchestration:
- CI/CD:
- Observability:
- Security:

## 8. Testing and Quality
- Test Strategy:
- Quality Gates:

## 9. Versioning and Documentation
- API Versioning:
- Schema/Migrations:
- Technical Docs:

## 10. Open Decisions
-

## 11. Risks and Mitigations
-
