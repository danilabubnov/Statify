package event

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant
import java.util.UUID

data class UserConnectedEvent(

    @JsonAlias("event_id")
    val eventId: UUID,

    @JsonAlias("user_id")
    val userId: UUID,

    @JsonAlias("event_type")
    val eventType: String,

    @JsonAlias("timestamp")
    val timestamp: Instant,

    @JsonAlias("metadata")
    val metadata: UserConnectedMetadata

)

data class UserConnectedMetadata(

    @JsonAlias("spotify_id")
    val spotifyId: String,

    @JsonAlias("access_token")
    val accessToken: String,

    @JsonAlias("refresh_token")
    val refreshToken: String

)