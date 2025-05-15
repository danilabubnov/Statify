package org.danila.event

import java.util.*

data class EnrichTrackEvent(
    override val eventId: UUID,
    override val userId: UUID,
    override val metadata: EnrichMetadata,
    val trackIds: Set<String>,
) : EnrichEvent