# DLS Stream Console (Frontend)

TypeScript + React test UI for all seven microservices.

## Run locally (Vite dev server)

Start backend services first (Docker Compose or individual services), then:

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:3000 — API calls are proxied to each service (see `vite.config.ts`).

## Run via Docker Compose

From repo root:

```bash
docker compose -f infra/docker/docker-compose.yml up --build frontend
```

The frontend container serves on http://localhost:3000 and proxies `/api/*` to backend containers.

## Pages

| Page | Service | Port |
|------|---------|------|
| Overview | All (health + demo flow) | — |
| Catalog | catalog-service (GraphQL) | 8082 |
| Playback | streaming-service | 8083 |
| Billing | billing-service | 8084 |
| Reviews | review-rating-service | 8085 |
| Notifications | engagement-service | 8086 |
| Recommendations | recommendation-service | 8090 |

Auth uses **user-service** (`POST /api/v1/auth/login` / `register`). JWT is stored in `localStorage` and sent to protected routes.

## Demo flow

The Overview page can run an end-to-end test: subscribe → play content → rate/review → ingest interaction → queue email → fetch recommendations.

Seeded catalog content IDs and billing plan IDs are built into the UI for quick testing.
