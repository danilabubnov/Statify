package event

import java.util.UUID

data class UserSpotifyLibraryStatusUpdatedEvent(
    val id: UUID,
    val status: UserLibraryStatus,
)