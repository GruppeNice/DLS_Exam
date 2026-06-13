# Catalog Service Architecture Overview

GraphQL-based content metadata service for the DLS streaming platform.

## Responsibilities

- Own content metadata, availability rules, and search indexes
- Expose a query-oriented GraphQL API (CQRS read model)
- Publish catalog lifecycle events to RabbitMQ
- Consume review/rating events to maintain aggregate rating stats

## Stack

- Java 21, Spring Boot 3.5 (parent POM)
- Spring for GraphQL + GraphiQL
- Spring Data JPA + MySQL 8
- Flyway migrations
- Spring AMQP + RabbitMQ topic exchanges
- JWT validation (shared secret with User Service)

## Data ownership

| Table | Purpose |
|-------|---------|
| `content_items` | Movies and TV shows |
| `genres`, `tags` | Taxonomy |
| `content_genres`, `content_tags` | Associations |
| `cast_members` | Cast and crew |
| `content_availability` | Region-based availability |
| `content_rating_stats` | Aggregated ratings from review events |

## Events produced (`catalog.events`)

| Routing key | When |
|-------------|------|
| `content.created` | New content metadata saved |
| `content.updated` | Metadata changed |
| `content.removed` | Soft-delete tombstone |

## Events consumed (`review.events`)

| Routing key | Action |
|-------------|--------|
| `content.rated` | Update average rating |
| `content.reviewed` | Increment review count |

Queue: `catalog-service.review-events`

## Patterns

- **CQRS**: GraphQL query API; mutations for metadata commands
- **Tombstone pattern**: `deleted` flag + `content.removed` event with `tombstone: true`
- **Database-per-service**: dedicated `catalog_db` on port `3310`

## Local ports

| Component | Port |
|-----------|------|
| Catalog Service | 8082 |
| MySQL | 3310 |
| RabbitMQ | 5672 |
