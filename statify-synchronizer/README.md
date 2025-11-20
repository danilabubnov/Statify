# statify-synchronizer

Asynchronous ETL service for fetching and storing user's Spotify library data. Consumes authentication events from Kafka, fetches user data from Spotify API, enriches metadata via MusicBrainz, and stores it in PostgreSQL.

## Overview

statify-synchronizer is a reactive, event-driven service that:
- Consumes `UserSpotifyConnectedEvent` from Kafka
- Fetches user's saved tracks, albums, and followed artists from Spotify API
- Enriches album metadata with MusicBrainz release groups
- Stores normalized data in PostgreSQL using R2DBC (reactive)
- Caches synchronization state in Redis
- Publishes sync status updates to Kafka

Built with Kotlin coroutines and Spring WebFlux for high-throughput, non-blocking operations.

## Technology Stack

- **Kotlin** + **Coroutines** - Asynchronous programming
- **Spring Boot 3** + **Spring WebFlux** - Reactive framework
- **Spring Data R2DBC** - Reactive PostgreSQL access
- **Spring Kafka (Reactive)** - Event streaming
- **Redis** - Synchronization state and caching
- **Retrofit + OkHttp** - HTTP clients for Spotify & MusicBrainz APIs
- **Resilience4j** - Circuit breaker and retry logic
- **Prometheus** - Metrics and observability

## Architecture

```
Kafka (user-spotify-connected)
    ↓
UserConnectedHandler
    ↓
SpotifyService.fetchSpotifyData()
    ├─ Fetch Saved Tracks (paginated)
    ├─ Fetch Saved Albums (paginated)
    └─ Fetch Followed Artists (paginated)
    ↓
Store in PostgreSQL (R2DBC)
    ├─ Tracks, Albums, Artists (deduplicated)
    ├─ User favorites (many-to-many)
    └─ Images, Genres, Artist relations
    ↓
Publish Enrich Events (Kafka)
    ├─ TRACK_ENRICH_TOPIC
    ├─ ALBUM_ENRICH_TOPIC (triggers MusicBrainz lookup)
    └─ ARTIST_ENRICH_TOPIC
    ↓
EnrichHandler (Generation 1+)
    ├─ Fetch missing related entities
    └─ MusicBrainz Album Enrichment
         ├─ Lookup by Barcode (UPC)
         └─ Lookup by Name + Artist
    ↓
Store enriched data → Update status
    ↓
Kafka (user-spotify-library-status-updated)
```

---

## Data Synchronization Flow

### Phase 1: Initial Fetch (Generation 0)

1. **Event Consumption**
   - Consumes `UserSpotifyConnectedEvent` from Kafka topic `user-spotify-connected`
   - Event contains: `userId`, `spotifyAccessToken`, `spotifyRefreshToken`

2. **Spotify API Fetching**

   **Saved Tracks:**
   - Endpoint: `GET /v1/me/tracks` (paginated, 50 items/page)
   - Extracts: Track, Album, Artists, Images
   - Batches: Processes in parallel batches of 50 tracks
   - Stores:
     - `tracks` table (id, name, duration_ms, explicit, popularity, preview_url)
     - `user_favorite_tracks` (user_id, track_id, added_at)
     - `albums` table (id, name, album_type, release_date, label, total_tracks)
     - `artists` table (id, name, popularity, followers_total)
     - `track_artists`, `album_artists` (many-to-many relations)
     - `album_images`, `artist_images` (image URLs and dimensions)

   **Saved Albums:**
   - Endpoint: `GET /v1/me/albums` (paginated, 50 items/page)
   - Similar storage pattern as tracks
   - Stores: Albums + Artists + Images
   - Links: `user_favorite_albums` table

   **Followed Artists:**
   - Endpoint: `GET /v1/me/following?type=artist` (paginated, 50 items/page)
   - Stores: Artist metadata, genres, images
   - Links: `user_followed_artists` table

3. **Deduplication**
   - Uses PostgreSQL `INSERT ... ON CONFLICT DO UPDATE`
   - Tracks/Albums/Artists identified by Spotify ID
   - Updates popularity and follower counts if changed

4. **Event Publishing**

   For each entity type (tracks, albums, artists):
   - Publishes enrichment events to Kafka:
     - `TRACK_ENRICH_TOPIC`
     - `ALBUM_ENRICH_TOPIC`
     - `ARTIST_ENRICH_TOPIC`
   - Events contain: entity IDs, user ID, correlation ID, generation = 0

5. **Status Update**
   - If enrichment needed: Status = `PARTIALLY_SYNCED`
   - If no enrichment needed: Status = `COMPLETED`
   - Publishes `UserSpotifyLibraryStatusUpdatedEvent` to Kafka

---

### Phase 2: Enrichment (Generation 1)

#### 2.1 Track & Artist Enrichment

**Purpose:** Fetch missing related entities that weren't included in user's library.

For example:
- User saved a track → Need to fetch full album details
- User saved an album → Need to fetch all album tracks

**Process:**
- Consumes events from `*_ENRICH_TOPIC`
- Groups by correlation ID and generation
- Batches requests to Spotify API:
  - `GET /v1/tracks?ids=...` (max 50 IDs)
  - `GET /v1/albums?ids=...` (max 20 IDs)
  - `GET /v1/artists?ids=...` (max 50 IDs)
- Stores enriched entities in PostgreSQL

#### 2.2 Album Enrichment with MusicBrainz

**Why MusicBrainz?**

Spotify API provides limited album metadata. MusicBrainz is an open music encyclopedia that provides:
- **Release Groups** - Canonical album identifiers across different editions (CD, Vinyl, Deluxe, etc.)
- **Rich Metadata** - Label, country, release date, album type
- **Canonical Identifiers** - Enables deduplication of album variants

**Example Use Case:**
- Spotify has separate entries for:
  - "Abbey Road (2009 Remaster)"
  - "Abbey Road (50th Anniversary Edition)"
  - "Abbey Road (Original 1969 Release)"
- MusicBrainz links all to the same **Release Group**: The Beatles' "Abbey Road" (1969)

**Lookup Process:**

1. **Batch Preparation**
   - Albums without `mb_release_group_id` are batched (100 albums/batch)
   - Published to:
     - `ALBUM_RG_LOOKUP_BY_BARCODE_TOPIC` (for albums with UPC barcode)
     - `ALBUM_RG_LOOKUP_BY_NAME_TOPIC` (fallback)

2. **Lookup by Barcode (Primary Method)**
   - Endpoint: `GET https://musicbrainz.org/ws/2/release?query=barcode:{barcode}`
   - Barcode = UPC (Universal Product Code) from Spotify
   - Matches with ~80-90% accuracy
   - Extracts `release-group.id` from response

3. **Lookup by Name (Fallback Method)**
   - Endpoint: `GET https://musicbrainz.org/ws/2/release?query=release:{name} AND artist:{artist}`
   - Fuzzy matching:
     - Normalizes album and artist names (lowercase, removes special chars)
     - Checks if artist names intersect
     - Checks if release title starts with first 50% of album name
   - Lower accuracy (~50-70%) but covers albums without barcodes

4. **Storage**
   - Stores `mb_release_group_id` in `albums` table
   - Creates entries in `mb_release_groups` table:
     - `id` - MusicBrainz Release Group UUID
     - `lookup_type` - BY_BARCODE or BY_NAME
     - `stale` - Flag for periodic refresh (90 minutes)
     - Metadata cached for performance

5. **Rate Limiting**
   - MusicBrainz has strict rate limits (1 request/second)
   - Uses client-side rate limiting with semaphores
   - Batches requests to minimize API calls

---

### Phase 3: Completion

1. **Redis State Tracking**
   - Tracks pending Generation 1 enrichment tasks
   - Key: `pending_gen1:{correlationId}`
   - Decremented after each enrichment batch completes

2. **Final Status Update**
   - When `pending_gen1 == 0`: All enrichment complete
   - Publishes final status: `COMPLETED`
   - Frontend receives update via Kafka → WebSocket

---

## Kafka Topics

### Consumed

| Topic | Event Type | Trigger |
|-------|-----------|---------|
| `user-spotify-connected` | `UserSpotifyConnectedEvent` | User links Spotify account |
| `track-enrich`, `album-enrich`, `artist-enrich` | `EnrichEvent` | Missing entities need fetching |
| `album-rg-lookup-by-barcode`, `album-rg-lookup-by-name` | `AlbumReleaseGroupBatchEvent` | Albums need MusicBrainz enrichment |

### Produced

| Topic | Event Type | Description |
|-------|-----------|-------------|
| `track-enrich`, `album-enrich`, `artist-enrich` | `EnrichEvent` | Request enrichment for entities |
| `album-rg-lookup-by-barcode`, `album-rg-lookup-by-name` | `AlbumReleaseGroupBatchEvent` | Request MusicBrainz lookup |
| `user-spotify-library-status-updated` | `UserSpotifyLibraryStatusUpdatedEvent` | Sync status changed |

---

## Configuration

See `.env.example` for all required environment variables.

### Key Configuration

**Database (R2DBC):**
- Reactive PostgreSQL driver (non-blocking I/O)
- Connection pool: 10-20 connections
- Write permits: 15 concurrent (controlled by semaphore)
- Read permits: 5 concurrent

**Kafka:**
- Consumer group: `statify-synchronizer-connected-group`
- Manual offset commit (after successful processing)
- Auto-create topics: enabled
- Concurrency: 3-4 concurrent event streams

**HTTP Clients:**
- Timeout: 30 seconds (configurable)
- Circuit breaker:
  - Sliding window: 10 requests
  - Failure threshold: 70%
  - Open state duration: 20 seconds
  - Half-open attempts: 5

**Redis:**
- Used for correlation state tracking
- TTL for pending counters: 24 hours

---

## Running Locally

### Prerequisites
- JDK 21
- PostgreSQL 16 (R2DBC compatible)
- Kafka
- Redis
- Spotify Developer credentials

### Steps

1. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your values
   ```

2. **Start dependencies**
   ```bash
   # From root directory
   docker-compose -f docker-compose.local.yaml up -d postgres-data-storage kafka redis
   ```

3. **Run the service**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

4. **Verify**
   ```bash
   curl http://localhost:8082/actuator/health
   ```

5. **Trigger sync**
   - Link Spotify account via statify-core: `POST /api/oauth/link/spotify`
   - Watch logs for sync progress

---

## Database Schema

Managed by statify-data-api (Hibernate auto-generation) or manual schema creation.

**Key Tables:**

- `tracks` - Track metadata from Spotify
- `albums` - Album metadata (with optional `mb_release_group_id`)
- `artists` - Artist metadata
- `mb_release_groups` - MusicBrainz release group cache
- `user_favorite_tracks` - User's saved tracks (many-to-many)
- `user_favorite_albums` - User's saved albums (many-to-many)
- `user_followed_artists` - User's followed artists (many-to-many)
- `track_artists`, `album_artists` - Artist relations
- `track_images`, `album_images`, `artist_images` - Image URLs

**Indexes:**
- Spotify IDs (unique)
- User favorites (user_id + entity_id composite)
- Popularity, release date (for sorting/filtering)

---

## Error Handling

**Retry Strategy:**
- Exponential backoff: 2s, 4s, 8s
- Max retries: 3
- Circuit breaker protection

**DLT (Dead Letter Topic):**
- Failed events sent to `{topic}.dlt`
- Manual inspection and reprocessing

**Failure Scenarios:**

1. **Spotify API Rate Limit (429)**
   - Backs off for `Retry-After` seconds
   - Circuit breaker opens after repeated failures

2. **MusicBrainz Rate Limit (503)**
   - Client-side rate limiting (1 req/sec)
   - Retries with exponential backoff

3. **PostgreSQL Write Conflict**
   - Upsert handles concurrent writes safely
   - R2DBC retries transient failures

4. **Kafka Consumer Failure**
   - Manual commit ensures at-least-once delivery
   - Idempotent handlers prevent duplicate processing

**Status Updates:**
- On failure: Status = `FAILED`
- Frontend notified via Kafka

---

## Monitoring

**Prometheus Metrics:**
- `/actuator/prometheus` - Metrics endpoint
- Custom metrics:
  - `spotify_api_requests_total`
  - `musicbrainz_api_requests_total`
  - `batch_processing_duration_seconds`
  - `enrichment_pending_count`

**Health Checks:**
- `/actuator/health`
- Checks: PostgreSQL (R2DBC), Kafka, Redis

---

## Performance

**Throughput:**
- ~10000 entities/minute (Spotify API rate limit bound)
- Parallel batch processing (3-4 concurrent streams)
- Redis caching for intermediate state

**Optimization:**
- Batched Spotify API calls (50 IDs per request)
- Reactive streams (non-blocking I/O)
- Database write semaphores (prevent connection exhaustion)

---

## Build

```bash
./gradlew build
```

Docker image:
```bash
docker build -t statify-synchronizer .
```

---

## MusicBrainz Integration Summary

**Purpose:** Enrich Spotify albums with canonical release group IDs from MusicBrainz.

**Benefits:**
- Deduplicate album variants (remasters, deluxe editions)
- Link to open music metadata database
- Enable future features (release history, discography analysis)

**Lookup Methods:**
1. **By Barcode (UPC)** - Primary, high accuracy (~80-90%)
2. **By Name + Artist** - Fallback, fuzzy matching (~50-70%)

**Rate Limiting:**
- MusicBrainz: 1 request/second (client-side enforced)
- Batch processing to minimize API calls

**Caching:**
- Results cached in `mb_release_groups` table
- Stale flag for periodic refresh (90 minutes)
