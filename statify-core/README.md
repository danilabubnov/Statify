# Statify-core

**Statify-core** is a Kotlin-based microservice built with Spring Boot. It provides user registration, JWT authentication, and OAuth2 integration with Spotify.

## 🔧 Technologies Used

- Kotlin
- Spring Boot
- PostgreSQL
- Kafka
- OAuth2 (Spotify)
- Docker & Docker Compose

## 🚀 Getting Started

## 🔒 Security

- All protected endpoints require a valid JWT token passed in the `Authorization` header:

- JWT tokens are issued after a successful login via the `/login` endpoint.

- A refresh token is issued in a secure HttpOnly cookie.
    - Path is limited to /api/auth/refresh.
    - Can be used to obtain a new access token without re-login.

- Logout (/api/auth/logout) clears the refresh token cookie.

- Spotify OAuth2 linking (/api/oauth/link/spotify) is only available to authenticated users.

- Sensitive configuration values (e.g., secrets, keys, credentials) are injected via environment variables and should never be hardcoded.

### Authentication Flow

1. Register

```
POST /api/auth/register
```

Creates a new user.

2. Login

```
POST /api/auth/login
```

Response body contains an access token.
Response headers contain an HttpOnly cookie with the refresh token.

3. Refresh Access Token

```
POST /api/auth/refresh
```

Uses the refresh token from the HttpOnly cookie.
Responds with a new access token.

4. Logout

```
POST /api/auth/logout
```

Clears the refresh token cookie.

### Link Spotify Account

Initiates the OAuth2 authorization flow with Spotify for the authenticated user.

```
POST /api/oauth/link/spotify
Authorization: Bearer <access-token>
```

Behavior:

    Responds with a 302 Found redirect

    The Location header contains the Spotify authorization URL

    After successful authorization, statify-core emits a Kafka event to trigger statify-synchronizer for full user library sync

Example response headers:
HTTP/1.1 302 Found
Location: https://accounts.spotify.com/authorize?client_id=...

### Run with Docker Compose

```bash
git clone https://github.com/danilabubnov/Statify.git
cd statify-core
cp .env.example .env  # Fill in your environment variables
docker-compose up --build
```

📄 API documentation is available at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)