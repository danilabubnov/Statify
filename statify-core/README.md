# statify-core

Authentication and authorization service for the Statify platform. Handles user registration, login, JWT token management, and Spotify OAuth2 integration.

## Overview

statify-core is the authentication gateway for Statify. It manages:
- User registration and login with email/password
- JWT-based authentication (access + refresh tokens)
- Spotify OAuth2 linking for existing users
- Secure token storage in HTTP-only cookies
- Publishing authentication events to Kafka for downstream services

## Technology Stack

- **Kotlin** + **Spring Boot 3**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** + **PostgreSQL** - User data persistence
- **Spring Kafka** - Event publishing
- **JWT (jjwt)** - Token generation and validation
- **AES Encryption** - Sensitive data encryption (Spotify tokens)

## Architecture

```
Client → statify-core
           ├─ Authentication (JWT)
           ├─ User Management (PostgreSQL)
           ├─ Spotify OAuth2 Flow
           └─ Event Publishing (Kafka) → statify-synchronizer
```

## API Endpoints

### Authentication

#### `POST /api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "displayName": "John Doe"
}
```

**Response:**
- **201 Created** - User registered successfully
- Returns `accessToken` and user data
- Sets HTTP-only `refreshToken` cookie

---

#### `POST /api/auth/login`
Authenticate existing user.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response:**
- **200 OK** - Login successful
- Returns `accessToken` and user data
- Sets HTTP-only `refreshToken` cookie

---

#### `POST /api/auth/refresh`
Refresh an expired access token.

**Request:**
- Requires `refreshToken` cookie (automatically sent by browser)

**Response:**
```json
{
  "accessToken": "new_access_token_here"
}
```

---

#### `GET /api/auth/me`
Get authenticated user information.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "displayName": "John Doe",
  "spotifyLinked": true
}
```

---

#### `POST /api/auth/logout`
Logout the current user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
- **204 No Content** - Clears `refreshToken` cookie

---

### OAuth2

#### `POST /api/oauth/link/spotify`
Initiate Spotify OAuth2 linking for authenticated user.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response:**
- **303 See Other** - Redirects to Spotify authorization page
- After approval, redirects back to `{SERVER_URL}/login/oauth2/code/spotify`

**Flow:**
1. User calls this endpoint → Redirected to Spotify login
2. User approves → Spotify redirects to statify-core callback
3. statify-core exchanges code for tokens
4. Encrypted Spotify tokens stored in database
5. Event published to Kafka: `UserSpotifyConnectedEvent`
6. statify-synchronizer consumes event and starts data sync

---

## Configuration

See `.env.example` for all required environment variables.

### Key Configuration

**Database:**
- Uses PostgreSQL for user and OAuth2 state storage
- JPA with Hibernate for ORM
- Schema managed via migrations or `ddl-auto=none` in production

**JWT:**
- Access tokens expire in 15 minutes (configurable)
- Refresh tokens expire in 7 days (configurable)
- Stored in HTTP-only cookies for security

**Encryption:**
- Spotify access/refresh tokens encrypted at rest using AES
- Requires `ENCRYPTION_PASSWORD` and `ENCRYPTION_SALT`

**Kafka:**
- Publishes events: `UserSpotifyConnectedEvent`
- Consumed by statify-synchronizer to trigger data sync

---

## Running Locally

### Prerequisites
- JDK 21
- PostgreSQL 16
- Kafka (or use root `docker-compose.local.yaml`)

### Steps

1. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your values
   ```

2. **Start dependencies**
   ```bash
   # From root directory
   docker-compose -f docker-compose.local.yaml up -d postgres-core kafka
   ```

3. **Run the service**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

4. **Verify**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

---

## Database Schema

**Users Table:**
- `id` (UUID, PK)
- `email` (unique, not null)
- `password` (bcrypt hashed)
- `display_name`
- `spotify_access_token` (AES encrypted)
- `spotify_refresh_token` (AES encrypted)
- `spotify_linked` (boolean)

**OAuth2 Link State Table:**
- Temporary storage for OAuth2 state parameter
- Prevents CSRF attacks during OAuth flow

---

## Security Features

- **Password Hashing:** BCrypt with configurable strength
- **JWT Signing:** HMAC-SHA256 with secret keys
- **Token Encryption:** Spotify tokens encrypted with AES-256
- **HTTP-only Cookies:** Refresh tokens not accessible via JavaScript
- **CSRF Protection:** OAuth2 state parameter validation
- **Secure Cookies:** `Secure` and `SameSite=None` attributes for HTTPS

---

## Event Publishing

### `UserSpotifyConnectedEvent`

Published when a user successfully links their Spotify account.

**Kafka Topic:** `user-spotify-connected`

**Event Payload:**
```json
{
  "userId": "uuid",
  "spotifyAccessToken": "encrypted_token",
  "spotifyRefreshToken": "encrypted_token"
}
```

Consumed by statify-synchronizer to initiate library synchronization.

---

## Error Handling

- **400 Bad Request** - Invalid input (validation errors)
- **401 Unauthorized** - Missing or invalid access token
- **409 Conflict** - User already exists (registration)
- **500 Internal Server Error** - Unexpected errors

All errors return consistent JSON structure:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-11-20T12:00:00Z"
}
```

---

## Health Checks

- **Endpoint:** `/actuator/health`
- **Probes:** Liveness and readiness probes enabled
- **Dependencies:** Checks database and Kafka connectivity

---

## Build

```bash
./gradlew build
```

Docker image:
```bash
docker build -t statify-core .
```
