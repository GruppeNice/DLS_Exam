# Recommendation Service Architecture Overview

## Purpose

The Recommendation Service is the AI/ML microservice for the DLS project. It consumes behavioral events, builds preference models, and exposes query-based recommendation APIs.

Aligned with architecture docs:

- consumes playback, rating, and subscription events
- builds user preference models from interaction weights
- generates personalized "Recommended for You" lists
- calculates trending/popular rankings (commutative aggregation)
- periodic model retraining

---

## Folder structure

- `src/recommendation_service/`: Python application code
- `tests/`: pytest tests
- `api/`: OpenAPI and AsyncAPI contracts
- `config/`: environment templates
- `requirements.txt`, `Dockerfile`, `docker-compose.yml`, `README.md`

---

## Data model

- `user_content_interactions` — aggregated user/content weights from events
- `user_recommendations` — precomputed recommendation lists per user
- `trending_content` — commutative popularity scores
- `model_runs` — retraining audit trail

---

## ML pipeline

1. Events update interaction weights via `InteractionService`
2. `RecommenderService.retrain()` builds a user-content matrix (Pandas)
3. Scikit-learn `NMF` produces latent preference factors
4. Top unseen content IDs are stored in `user_recommendations`
5. Query API serves cached lists; falls back to trending when no model output exists

---

## Messaging

`RabbitMqConsumer` binds queue `recommendation-service.events` to:

- `streaming.events` playback routing keys
- `billing.events` `subscription.activated`
- `review.events` `content.rated` (future producer)

`EventHandler` maps event payloads to interaction weight updates.

---

## REST API (query side of CQRS)

- `GET /api/v1/recommendations/me` — personalized recommendations (JWT)
- `GET /api/v1/recommendations/trending` — global trending list
- `POST /api/v1/recommendations/retrain` — manual retrain trigger

---

## Local runtime

- App: `8090`
- PostgreSQL: `5435`
- RabbitMQ: `5675` / UI `15675`
- Retrain interval: `MODEL_RETRAIN_INTERVAL_MINUTES` (default 30)

---

## Maturity notes

Future enhancements:

- consume from unified platform broker via `infra/messaging`
- add Ollama-based explainability or content descriptions
- Testcontainers + pytest integration suite
- Bandit security scan in CI
- stronger rating event contract once Review Service exists
