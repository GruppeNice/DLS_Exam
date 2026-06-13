# Recommendation Service

Python AI recommendation microservice implementing:

- RabbitMQ consumption of playback, subscription, and rating events
- user-content interaction tracking (commutative aggregation)
- collaborative filtering recommendations with Scikit-learn NMF
- trending/popular content rankings
- periodic model retraining (APScheduler)
- FastAPI REST query API (`/api/v1/recommendations/me`, `/trending`)
- JWT validation (tokens from User Service)

## Stack

- Python 3.12
- FastAPI + Uvicorn
- SQLAlchemy + PostgreSQL
- Pandas + NumPy + Scikit-learn
- Pika (RabbitMQ)
- PyJWT
- Pytest

## Run locally

```bash
cd services/recommendation-service
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
set PYTHONPATH=src
uvicorn recommendation_service.main:app --reload --port 8090 --app-dir src
```

Requires PostgreSQL and RabbitMQ (or use shared Docker Compose).

## Docker

From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build recommendation-service recommendation-service-db rabbitmq
```

Or from this folder:

```bash
docker compose up --build recommendation-service recommendation-service-db rabbitmq
```

This starts:

- `recommendation-service` on `http://localhost:8090`
- PostgreSQL on `localhost:5435`
- Shared RabbitMQ on `localhost:5672` / UI `http://localhost:15672`

API docs: `http://localhost:8090/docs`

## Consumed events

| Routing key | Source exchange | Purpose |
|-------------|-----------------|---------|
| `playback.started` | `streaming.events` | Initial watch signal |
| `playback.progress.updated` | `streaming.events` | Progress-weighted preference |
| `playback.stopped` | `streaming.events` | Session completion signal |
| `subscription.activated` | `billing.events` | Subscription context |
| `content.rated` | `review.events` | Explicit rating signal |

For local end-to-end event flow, point `RABBITMQ_*` at the same broker used by Streaming/Billing, or use the manual ingest endpoint for testing.

## API overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/health` | Public | Health check |
| GET | `/api/v1/recommendations/me` | JWT | Personalized list |
| GET | `/api/v1/recommendations/trending` | Public | Trending content |
| POST | `/api/v1/recommendations/retrain` | Public | Trigger retraining |
| POST | `/api/v1/recommendations/interactions` | Public | Dev/test interaction ingest |
