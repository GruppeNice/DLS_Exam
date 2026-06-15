# Review & Rating Service

Spring Boot microservice implementing:

- star ratings (1–5) and written reviews for catalog content
- helpful votes on reviews
- RabbitMQ domain events (`content.rated`, `content.reviewed`) on exchange `review.events`
- Flyway database migrations with seed reviews aligned to demo user and catalog IDs
- Micrometer metrics and Zipkin tracing (parent POM)

## Run locally

```bash
# from repository root
mvn spring-boot:run -pl services/review-rating-service
```

## Docker

From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build review-rating-service review-rating-service-db rabbitmq
```

Or from this folder:

```bash
docker compose up --build review-rating-service review-rating-service-db rabbitmq
```

This starts:

- `review-rating-service` on `http://localhost:8085`
- MySQL `review_db` on `localhost:3311`
- Shared RabbitMQ on `localhost:5672` / UI `http://localhost:15672`

## API overview

Paths are exposed at the service root (nginx strips `/api/review` in Docker).

| Method | Path | Description |
|--------|------|-------------|
| POST | `/ratings/add` | Submit a star rating |
| GET | `/ratings/{ratingId}` | Fetch a rating by id |
| POST | `/reviews/add` | Submit a written review |
| GET | `/reviews/{reviewId}` | Fetch a review by id |
| GET | `/reviews/movie/{movieId}` | List reviews for content |
| POST | `/review-votes` | Record a helpful vote on a review |

## Events produced

| Routing key | Exchange | Consumers |
|-------------|----------|-----------|
| `content.rated` | `review.events` | Catalog (rating stats), Recommendation |
| `content.reviewed` | `review.events` | Catalog, Engagement |

AsyncAPI stub: `api/asyncapi.yaml`. Canonical contract: `packages/contracts/asyncapi/dls-platform-events.yaml`.

## Database seeds

Flyway `V2__seed_reviews.sql` and `V3__seed_laugh_track_reviews.sql` populate sample ratings/reviews for catalog seed IDs and `demo@dls.local`.
