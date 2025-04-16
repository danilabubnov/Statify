package event

import java.time.Instant
import java.util.*

data class UserConnectedEvent(
    val eventId: UUID,
    val userId: UUID,
    val timestamp: Instant,
    val metadata: UserMetadata
)