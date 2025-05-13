package org.danila.event

import event.TokenCredentials

data class EnrichMetadata(val tokenCredentials: TokenCredentials, val correlationId: String, val generation: Int)