# Catalog Service (GraphQL)

Spring Boot catalog microservice implementing:

- movie and TV show metadata management
- genres, tags, cast, and region-based availability
- GraphQL query API for content discovery and search (CQRS query side)
- JWT-protected GraphQL mutations for create, update, and remove
- tombstone-style `content.removed` events
- RabbitMQ domain events (`content.created`, `content.updated`, `content.removed`)
- consumption of `content.rated` and `content.reviewed` from `review.events`
- Flyway database migrations with seed catalog data

## Run locally

```bash
# from repository root
mvn spring-boot:run -pl services/catalog-service
```

## Docker

From the **repository root**:

```bash
docker compose -f infra/docker/docker-compose.yml up --build catalog-service catalog-service-db rabbitmq
```

Or from this folder:

```bash
docker compose up --build catalog-service catalog-service-db rabbitmq
```

This starts:

- `catalog-service` on `http://localhost:8082`
- GraphQL endpoint `http://localhost:8082/graphql`
- GraphiQL UI `http://localhost:8082/graphiql`
- MySQL on `localhost:3310`
- Shared RabbitMQ on `localhost:5672` / UI `http://localhost:15672`

## GraphQL overview

### Queries (public)

| Query | Description |
|-------|-------------|
| `contentById` | Fetch a single content item with nested genres, tags, cast, availability, and ratings |
| `searchContent` | Filter by title, genre, tag, content type, and region |
| `genres` | List all genres |
| `tags` | List all tags |

### Mutations (JWT required)

| Mutation | Description |
|----------|-------------|
| `createContent` | Create metadata and publish `content.created` |
| `updateContent` | Update metadata and publish `content.updated` |
| `removeContent` | Soft-delete (tombstone) and publish `content.removed` |

### Example query

```graphql
query {
  searchContent(filter: { regionCode: "US", titleContains: "Neon" }, page: 0, size: 10) {
    totalCount
    items {
      id
      title
      contentType
      availableInRegion(regionCode: "US")
      ratingStats {
        averageRating
        ratingCount
      }
    }
  }
}
```

### Seeded content IDs (for playback tests)

| Title | ID |
|-------|-----|
| Neon Horizon | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1` |
| Echoes of Mars | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2` |
| Midnight Express | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3` |

JWT tokens are issued by the User Service and validated using the shared secret.
