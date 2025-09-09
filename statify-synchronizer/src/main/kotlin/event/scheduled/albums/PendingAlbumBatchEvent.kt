package org.danila.event.scheduled.albums

import java.util.*

data class PendingAlbumBatchEvent(
    val eventId: UUID,
    val ids: List<String>,
)
