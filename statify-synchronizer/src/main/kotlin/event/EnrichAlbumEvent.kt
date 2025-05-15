package org.danila.event

import java.util.*

data class EnrichAlbumEvent(
    override val eventId: UUID,
    override val userId: UUID,
    override val metadata: EnrichMetadata,
    val albumIds: Set<String>,
) : EnrichEvent