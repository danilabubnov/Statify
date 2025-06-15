package org.danila.event.spotifyfetch

import java.util.UUID

data class SpotifyFetchContext(
    val libraryId: UUID,
    val generation: Int,
    val correlationId: String,
)
