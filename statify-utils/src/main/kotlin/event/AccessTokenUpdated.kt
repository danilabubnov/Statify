package event

import java.util.*

data class AccessTokenUpdatedEvent(
    val eventId: UUID,
    val spotifyId: String,
    val accessToken: String
)