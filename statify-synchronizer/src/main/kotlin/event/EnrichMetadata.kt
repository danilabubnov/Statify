package org.danila.event

import event.TokenCredentials
import java.util.*

data class EnrichMetadata(val tokenCredentials: TokenCredentials, val correlationId: String, val generation: Int, val userSpotifyLibraryId: UUID)