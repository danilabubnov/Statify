# statify-data-api

GraphQL API service for querying user's Spotify library data and analytics. Provides read-only access to synchronized music data stored by statify-synchronizer.

## Overview

statify-data-api exposes a GraphQL API for:
- User's favorite tracks and albums (saved library)
- Top tracks by popularity
- Top artists by popularity
- Top albums by popularity
- Paginated results with cursor and offset-based pagination

Data is read from PostgreSQL, populated by statify-synchronizer after Spotify OAuth2 linking.

## Technology Stack

- **Kotlin** + **Spring Boot 3**
- **Netflix DGS GraphQL** - GraphQL server framework
- **Spring Data JPA** + **PostgreSQL** - Data access layer
- **JWT Authentication** - Validates tokens issued by statify-core
- **Flyway** - Database schema migrations

## Architecture

```
Client (with JWT) → statify-data-api
                      ├─ GraphQL API
                      ├─ JWT Validation (statify-core tokens)
                      └─ PostgreSQL (read-only queries)
                            ↑
                    statify-synchronizer (writes data)
```

## GraphQL API

### Endpoint

- **URL:** `/graphql`
- **Method:** POST
- **Authentication:** Required (JWT Bearer token from statify-core)

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Exploring the API

Use [GraphiQL](https://localhost/api/data/graphiql) (when running via Docker with Nginx) or [Apollo Studio Sandbox](https://studio.apollographql.com/sandbox/explorer) for interactive exploration.

---

## Queries

### User's Favorite Tracks

Retrieve the authenticated user's saved (favorite) tracks from Spotify.

```graphql
query GetMyFavoriteTracks($size: Int, $offset: Int) {
  getMyFavoriteTracks(size: $size, offset: $offset) {
    items {
      id
      name
      durationMs
      explicit
      addedAt
      artists {
        id
        name
        popularity
        followersTotal
      }
      covers {
        imageUrl
        imageHeight
        imageWidth
      }
      album {
        id
        name
      }
    }
    totalCount
    hasMore
  }
}
```

**Variables:**
```json
{
  "size": 20,
  "offset": 0
}
```

**Parameters:**
- `size` - Number of items per page (1-100, default: 20)
- `offset` - Offset for pagination (0-based, default: 0)

---

### User's Favorite Albums

Retrieve the authenticated user's saved albums from Spotify.

```graphql
query GetMyFavoriteAlbums($size: Int, $after: ID) {
  getMyFavoriteAlbums(size: $size, after: $after) {
    edges {
      node {
        id
        name
        albumType
        totalTracks
        label
        popularity
        releaseDate
        addedAt
      }
      cursor
    }
    pageInfo {
      hasNextPage
      hasPreviousPage
      startCursor
      endCursor
    }
  }
}
```

**Variables:**
```json
{
  "size": 10,
  "after": null
}
```

**Parameters:**
- `size` - Number of items per page (1-20, default: 10)
- `after` - Cursor for pagination (cursor-based)

---

### Top Tracks by Popularity

Get top tracks sorted by Spotify popularity score.

```graphql
query TopTracks($page: Int!, $size: Int, $from: Date, $to: Date) {
  topTracksByPopularity(page: $page, size: $size, from: $from, to: $to) {
    items {
      id
      name
      artists {
        id
        name
        popularity
      }
      covers {
        imageUrl
        imageHeight
        imageWidth
      }
    }
    pageInfo {
      page
      size
      hasNextPage
      hasPreviousPage
    }
    totalCount
  }
}
```

**Variables:**
```json
{
  "page": 0,
  "size": 25,
  "from": "2024-01-01",
  "to": "2024-12-31"
}
```

**Parameters:**
- `page` - Page number (0-based, required)
- `size` - Items per page (1-50, default: 25)
- `from` - Filter tracks added after this date (optional)
- `to` - Filter tracks added before this date (optional)

---

### Top Artists by Popularity

Get top artists sorted by Spotify popularity and follower count.

```graphql
query TopArtists($page: Int!, $size: Int) {
  topArtistsByPopularity(page: $page, size: $size) {
    items {
      id
      name
      images {
        imageUrl
        imageHeight
        imageWidth
      }
    }
    pageInfo {
      page
      size
      hasNextPage
      hasPreviousPage
    }
    totalCount
  }
}
```

**Variables:**
```json
{
  "page": 0,
  "size": 50
}
```

**Parameters:**
- `page` - Page number (0-based, required)
- `size` - Items per page (1-100, default: 50)

---

### Top Albums by Popularity

Get top albums sorted by popularity, with optional filtering by album type.

```graphql
query TopAlbums($page: Int!, $size: Int, $albumType: AlbumType) {
  topAlbumsByPopularity(page: $page, size: $size, albumType: $albumType) {
    items {
      id
      name
      artists {
        id
        name
      }
      covers {
        imageUrl
        imageHeight
        imageWidth
      }
    }
    pageInfo {
      page
      size
      hasNextPage
      hasPreviousPage
    }
    totalCount
  }
}
```

**Variables:**
```json
{
  "page": 0,
  "size": 25,
  "albumType": "ALBUM"
}
```

**Parameters:**
- `page` - Page number (0-based, required)
- `size` - Items per page (1-50, default: 25)
- `albumType` - Filter by type: `ALBUM`, `SINGLE`, `COMPILATION` (default: `ALBUM`)

---

## Authentication

All GraphQL queries require a valid JWT access token from statify-core.

**Example:**
```bash
curl -X POST https://localhost/api/data/graphql \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { getMyFavoriteTracks(size: 5, offset: 0) { items { id name } } }"
  }'
```

If the token is missing or invalid:
```json
{
  "errors": [
    {
      "message": "Unauthorized",
      "extensions": {
        "classification": "UNAUTHORIZED"
      }
    }
  ]
}
```

---

## Data Model

### Types

**FavTrack** - User's saved track
- `id`, `name`, `durationMs`, `explicit`, `addedAt`
- `artists` - List of artists (id, name, popularity, followers)
- `covers` - Album cover images
- `album` - Basic album info (id, name)

**FavAlbum** - User's saved album
- `id`, `name`, `albumType`, `totalTracks`, `label`, `popularity`, `releaseDate`, `addedAt`

**TrackPreview** - Track summary (for top tracks)
- `id`, `name`
- `artists` - Artist summaries
- `covers` - Album covers

**ArtistPreview** - Artist summary
- `id`, `name`
- `images` - Artist images

**AlbumPreview** - Album summary
- `id`, `name`
- `artists` - Artist summaries
- `covers` - Album covers

---

## Configuration

See `.env.example` for all required environment variables.

**Key Configuration:**

- **Database:** JDBC connection to `postgres-data-storage` (shared with statify-synchronizer)
- **JWT:** Uses same `JWT_ACCESS_SECRET_KEY` as statify-core for token validation
- **Port:** Runs on port 8081 by default

---

## Running Locally

### Prerequisites
- JDK 21
- PostgreSQL 16 with synced data from statify-synchronizer
- Access token from statify-core

### Steps

1. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your values
   ```

2. **Start PostgreSQL**
   ```bash
   # From root directory
   docker-compose -f docker-compose.local.yaml up -d postgres-data-storage
   ```

3. **Run the service**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

4. **Verify**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

5. **Test GraphQL**

   Get a JWT token from statify-core first:
   ```bash
   TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com","password":"password"}' \
     | jq -r '.accessToken')

   curl -X POST http://localhost:8081/graphql \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"query":"{ getMyFavoriteTracks(size:5,offset:0) { items { name } } }"}'
   ```

---

## Database Schema

Managed by Flyway migrations in `src/main/resources/db/migration/`.

**Key Tables:**
- `users` - User information (joined with statify-core)
- `tracks` - Track metadata
- `albums` - Album metadata
- `artists` - Artist metadata
- `user_favorite_tracks` - User's saved tracks (many-to-many)
- `user_favorite_albums` - User's saved albums (many-to-many)
- `mb_release_groups` - MusicBrainz enrichment data

**Indexes:**
- Optimized for sorting by popularity, release date, and added_at
- Foreign key indexes for efficient joins

---

## Error Handling

GraphQL errors follow DGS conventions:

**Validation Error:**
```json
{
  "errors": [
    {
      "message": "Validation failed for field 'size': must be between 1 and 100",
      "extensions": {
        "classification": "BAD_REQUEST"
      }
    }
  ]
}
```

**Unauthorized:**
```json
{
  "errors": [
    {
      "message": "Unauthorized",
      "extensions": {
        "classification": "UNAUTHORIZED"
      }
    }
  ]
}
```

---

## Health Checks

- **Endpoint:** `/actuator/health`
- **Dependencies:** Checks PostgreSQL connectivity

---

## Build

```bash
./gradlew build
```

Docker image:
```bash
docker build -t statify-data-api .
```
