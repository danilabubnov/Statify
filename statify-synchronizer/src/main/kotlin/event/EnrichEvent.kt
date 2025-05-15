package org.danila.event

import java.util.*

interface EnrichEvent {
    val eventId: UUID
    val userId: UUID
    val metadata: EnrichMetadata
}