package org.danila.event

import java.util.*

data class EnrichAlbumEvent(val eventId: UUID, val albumIds: Set<String>, val metadata: EnrichMetadata)