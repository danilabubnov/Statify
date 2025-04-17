package org.danila.event

import java.util.*

data class EnrichArtistEvent(override val eventId: UUID, val artistIds: Set<String>, override val metadata: EnrichMetadata) : EnrichEvent