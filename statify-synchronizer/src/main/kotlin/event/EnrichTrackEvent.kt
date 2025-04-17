package org.danila.event

import java.util.*

data class EnrichTrackEvent(override val eventId: UUID, val trackIds: Set<String>, override val metadata: EnrichMetadata) : EnrichEvent