package org.danila.mapper.graphql

import org.danila.generated.types.ArtistPreview
import org.danila.generated.types.ArtistSimple
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