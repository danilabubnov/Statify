package org.danila.event

import java.util.*

data class EnrichAlbumEvent(override val eventId: UUID, val albumIds: Set<String>, override val metadata: EnrichMetadata) : EnrichEvent