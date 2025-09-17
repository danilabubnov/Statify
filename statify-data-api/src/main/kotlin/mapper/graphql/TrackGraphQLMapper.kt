package org.danila.mapper.graphql

import org.danila.generated.types.TrackDTO
import org.danila.generated.types.TrackPreview
import org.danila.generated.types.TrackSimple
import org.danila.model.spotify.track.Track

fun Track.toTrackPreview() = TrackPreview(
    id = spotifyId,
    name = name,
    artists = artists.map { it.toArtistSimple() },
    covers = album.images.map { it.toImage() },
)

fun Track.toTrackSimple() = TrackSimple(
    id = spotifyId,
    name = name,
    durationMs = durationMs,
    explicit = explicit,
    trackNumber = trackNumber
)

fun Track.toTrackDTO() = TrackDTO(
    id = spotifyId,
    name = name,
    durationMs = durationMs,
    explicit = explicit,
    trackNumber = trackNumber,
    albumId = album.spotifyId
)