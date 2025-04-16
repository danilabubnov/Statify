package event

import java.time.Instant

data class UserMetadata(
    val spotifyId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant
)