package org.danila.mapper.graphql

import org.danila.generated.types.AlbumSimple
import org.danila.generated.types.ArtistDTO
import org.danila.generated.types.ArtistPreview
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.TrackSimple
import org.danila.model.spotify.artist.Artist

fun Artist.toArtistPreview() = ArtistPreview(
    id = spotifyId,
    name = name,
    images = images.map { it.toImage() }
)

fun Artist.toArtistSimple() = ArtistSimple(
    id = spotifyId,
    name = name,
    followersTotal = followersTotal
)

fun Artist.toArtistDTO(topTracks: List<TrackSimple>, albums: List<AlbumSimple>) = ArtistDTO(
    id = spotifyId,
    name = name,
    followersTotal = followersTotal,
    topTracks = topTracks,
    albums = albums
)