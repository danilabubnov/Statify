package org.danila.event.enrich

import java.util.*

interface EnrichEvent {
    val eventId: UUID
    val userId: UUID
    val metadata: EnrichMetadata
}