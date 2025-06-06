package event

import java.util.*

data class UserSpotifyLibraryStatusUpdatedEvent(
    val id: UUID,
    val status: UserLibraryStatus,
)