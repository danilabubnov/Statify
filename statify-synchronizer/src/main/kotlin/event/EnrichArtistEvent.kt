package org.danila.event

import java.util.*

data class EnrichArtistEvent(val eventId: UUID, val artistIds: Set<String>, val metadata: EnrichMetadata)