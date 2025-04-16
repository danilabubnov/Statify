package org.danila.event

import java.util.*

data class EnrichTrackEvent(val eventId: UUID, val trackIds: Set<String>, val metadata: EnrichMetadata)