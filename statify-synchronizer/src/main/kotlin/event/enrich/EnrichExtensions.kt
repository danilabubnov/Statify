package org.danila.event.enrich

object EnrichExtensions {

    fun EnrichEvent.functionName(): String = when (this) {
        is EnrichArtistEvent -> "enrich_artists"
        is EnrichTrackEvent -> "enrich_tracks"
        is EnrichAlbumEvent -> "enrich_albums"
        else -> "enrich_unknown"
    }

}