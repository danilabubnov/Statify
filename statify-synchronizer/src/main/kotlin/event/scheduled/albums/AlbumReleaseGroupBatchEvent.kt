package org.danila.event.scheduled.albums

import java.util.*

data class AlbumReleaseGroupBatchEvent(
    val eventId: UUID,
    val ids: List<String>,
    val lookupType: LookupType
)

enum class LookupType {
    BY_BARCODE, BY_NAME
}