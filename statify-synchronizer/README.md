# Statify-synchronizer

**Statify-synchronizer** is an asynchronous ETL microservice built with Kotlin, Spring Boot, and Coroutines. It listens for OAuth2 success events via Kafka, fetches user data from the Spotify Web API (library, tracks, albums, artists), and writes it to a PostgreSQL database using reactive R2DBC.

## 🔧 Technologies Used

- Kotlin + Coroutines
- Spring Boot
- Kafka
- PostgreSQL (R2DBC)
- Redis
- Retrofit
- Resilience4j
- Docker & Docker Compose
- Prometheus metrics

## 🚀 Getting Started

### Run with Docker Compose

```bash
git clone https://github.com/danilabubnov/Statify.git
cd statify-synchronizer
cp .env.example .env  # Fill in your environment variables
docker-compose up --build
```

## 📡 API

This service **does not expose a public HTTP API**.

Instead, it operates entirely through Kafka. It listens for domain events (e.g., successful Spotify OAuth2 authentication) and then performs asynchronous data synchronization with the Spotify API.

### Kafka Integration

- **Consumes** events from Kafka topics (`user.spotify.connected.v1`)
- **Triggers** data fetching jobs for:
    - Saved tracks
    - Saved albums
    - Followed artists
- **Publishes** sync status or result events (`user.spotify.library.status.updated.v1`)

All Spotify requests are made asynchronously using Kotlin coroutines and handled with retry logic via Resilience4j.

## 📈 Roadmap

- [ ] AI-powered custom playlist generation based on analyzed user library
- [ ] Unit and integration test coverage

> ⚠️ **Important:** This service requires a pre-initialized PostgreSQL schema with all necessary tables.  
> 
> You can create it manually using the provided SQL schema file, or run the companion service responsible for schema generation (see below).
> 
> The required schema can be found in [`../schema.sql`](../schema.sql).
> 
> Alternatively, you can run the companion service (`statify-data-api`) which uses Hibernate to auto-generate the required schema.

