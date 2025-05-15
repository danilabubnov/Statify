package org.danila.event

import java.util.*

data class EnrichArtistEvent(
    override val eventId: UUID,
    override val userId: UUID,
    override val metadata: EnrichMetadata,
    val artistIds: Set<String>,
) : EnrichEvent