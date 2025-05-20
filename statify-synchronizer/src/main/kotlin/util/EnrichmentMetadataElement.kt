package org.danila.util

import org.danila.event.EnrichMetadata
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

object EnrichmentMetadataKey : CoroutineContext.Key<EnrichmentMetadataElement>

class EnrichmentMetadataElement(val metadata: EnrichMetadata) :
    AbstractCoroutineContextElement(EnrichmentMetadataKey)