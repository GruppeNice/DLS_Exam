# DLS Platform — RabbitMQ Messaging

Central reference for the event-driven layer. Service code declares exchanges/queues at startup via Spring `RabbitAdmin` or the Python consumer.

## Canonical spec

| Artifact | Location |
|----------|----------|
| Full topology (human-readable) | [`TOPOLOGY.md`](./TOPOLOGY.md) |
| Broker import file | [`definitions.json`](./definitions.json) |
| Consolidated AsyncAPI | [`../../packages/contracts/asyncapi/dls-platform-events.yaml`](../../packages/contracts/asyncapi/dls-platform-events.yaml) |
| Per-service stubs | `services/*/api/asyncapi.yaml` |

## Broker (local)

| Item | Value |
|------|-------|
| Host | `rabbitmq` (compose) / `localhost` |
| AMQP port | `5672` |
| Management UI | http://localhost:15672 (`guest` / `guest`) |

## Import topology into RabbitMQ

With the broker running:

```bash
# RabbitMQ 3.x management API
curl -u guest:guest -H "content-type: application/json" \
  -X POST http://localhost:15672/api/definitions \
  --data-binary @infra/messaging/definitions.json
```

Services also declare their own exchanges/queues on startup, so import is optional for local dev.

## Runbook

1. Start platform: `docker compose -f infra/docker/docker-compose.yml up -d`
2. Publish a review → verify `review.events` fan-out in Management UI → Queues
3. Stop playback → confirm `engagement-service.domain-events` receives `playback.stopped`
4. Cross-check routing keys against `TOPOLOGY.md` when adding a new consumer
