package org.danila.event.spotifyfetch

data class SpotifyFetchCompletedEvent(
    val enrichmentRequired: Boolean,
    val spotifyFetchContext: SpotifyFetchContext,
)