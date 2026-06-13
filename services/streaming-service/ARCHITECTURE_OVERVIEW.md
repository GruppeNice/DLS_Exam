# Streaming Service Architecture Overview

This document explains what was created in `services/streaming-service` and what each file is responsible for.

## Purpose of this service

The Streaming / Playback Service manages playback session lifecycle for the DLS project. It provides:

- start, stop, and resume playback sessions
- watch progress tracking
- subscription validation before playback (via Billing Service REST check)
- simulated DRM validation
- playback-domain events published to RabbitMQ
- database schema management with Flyway

It aligns with the architecture docs: command-based streaming (CQRS), immutable playback events, and idempotent session start.

---

## Folder structure

- `src/main/java/com/dlsexam/streamingservice`: Java application code
- `src/main/resources`: application configuration and database migrations
- `src/test/java/com/dlsexam/streamingservice`: test code
- `api`: OpenAPI and AsyncAPI contracts
- `config`: runtime environment templates
- project root files (`pom.xml`, `Dockerfile`, `docker-compose.yml`, `README.md`)

---

## Domain model

- `PlaybackSession`: user playback session with content id, status, position, DRM token, and idempotency key

Statuses: `ACTIVE`, `STOPPED`, `COMPLETED`

---

## Core workflows

### Start playback

1. Resolve idempotent session by `Idempotency-Key` or create new
2. Call Billing Service `GET /api/v1/subscriptions/active/{userId}`
3. Run simulated DRM validation
4. Persist session and publish `PlaybackStarted`

### Stop / resume / progress

- Stop sets status to `STOPPED` and publishes `PlaybackStopped`
- Resume re-validates subscription + DRM, sets status to `ACTIVE`, publishes `PlaybackStarted`
- Progress update stores position and publishes `PlaybackProgressUpdated`

---

## Cross-service integration

`SubscriptionValidator` uses Spring `RestClient` to call the Billing Service public endpoint. This is a synchronous gate required before playback; async messaging is used for downstream consumers (Recommendation, Engagement).

---

## Messaging

`PlaybackEventPublisher` publishes:

- `playback.started`
- `playback.stopped`
- `playback.progress.updated`

Exchange: `streaming.events` (configurable)

---

## Local runtime

- App port: `8083`
- PostgreSQL: `5434`
- RabbitMQ: `5674` / management `15674`
- Billing dependency: `http://localhost:8084`

---

## Notes on current maturity

Future enhancements:

- full OpenAPI/AsyncAPI schemas
- Catalog Service content existence validation
- integration tests with Testcontainers (PostgreSQL, RabbitMQ, WireMock for Billing)
- shared API gateway routing
