package org.danila.mapper.graphql

import org.danila.generated.types.TrackPreview
import org.danila.model.spotify.track.Track

fun Track.toTrackPreview() = TrackPreview(
    id = spotifyId,
    name = name,
    artists = artists.map { it.toArtistSimple() },
    covers = album.images.map { it.toImage() },
)