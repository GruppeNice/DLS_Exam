# Shared Contracts

Cross-service API and event schemas for the DLS platform monorepo.

## Contents

| Path | Purpose |
|------|---------|
| [`asyncapi/dls-platform-events.yaml`](./asyncapi/dls-platform-events.yaml) | Consolidated AsyncAPI 2.6 spec for all RabbitMQ events |
| [`schemas/events.json`](./schemas/events.json) | JSON Schema definitions for event payloads |

## Relationship to per-service specs

Each service may keep a slim `services/<name>/api/asyncapi.yaml` stub that references or mirrors its slice of the platform contract. The file in this folder is the **single source of truth** for event shapes and routing keys.

## Messaging topology

Human-readable bindings: [`../../infra/messaging/TOPOLOGY.md`](../../infra/messaging/TOPOLOGY.md)

Broker import file: [`../../infra/messaging/definitions.json`](../../infra/messaging/definitions.json)

## Viewing AsyncAPI

Paste `asyncapi/dls-platform-events.yaml` into [AsyncAPI Studio](https://studio.asyncapi.com/) or use the AsyncAPI CLI:

```bash
npx @asyncapi/cli generate fromTemplate asyncapi/dls-platform-events.yaml @asyncapi/html-template -o asyncapi-docs
```

## Versioning

Event schemas use URI versioning in the `$id` field (`.../v1/...`). Breaking payload changes require a new major schema version and coordinated consumer updates.
