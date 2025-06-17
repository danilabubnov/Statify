# Statify Monorepo

Statify is a microservices-based system for Spotify data integration, analysis, and personalization.

## 🧱 Microservices

- [`statify-core`](./statify-core/) – Authentication, JWT, Spotify OAuth2 integration
- [`statify-synchronizer`](./statify-synchronizer/) – Async ETL service that fetches and stores user data from Spotify API

## 🐳 Running the System

```bash
cp .env.example .env
docker-compose up --build
```

Each service also includes its own .env.example file.

## 📂 Schema

The PostgreSQL schema used by `statify-synchronizer` is located in [`schema.sql`](schema.sql).

This schema must be applied to the database before running the synchronizer, as it defines all required tables and relationships for storing user data fetched from the Spotify API.

## 🐳 Running the System

To run the entire Statify system locally, use the provided `docker-compose.yml` in the root of the repository:

```bash
cp .env.example .env
docker-compose up --build
```

This will launch the following services:

    statify-core – Handles authentication, JWT issuance, and Spotify OAuth2.

    statify-synchronizer – Fetches user data from Spotify asynchronously after OAuth2.

    postgres-core – PostgreSQL database for statify-core.

    postgres-data-storage – PostgreSQL database for statify-synchronizer.

    kafka – Message broker for inter-service communication.

    redis – Used for caching and intermediate sync state.

    kafdrop – Web UI for inspecting Kafka topics: http://localhost:9000/

⚠️ Make sure you configure environment variables in .env before running the stack.