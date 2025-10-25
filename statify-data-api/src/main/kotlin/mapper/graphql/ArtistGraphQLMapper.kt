package org.danila.mapper.graphql

import org.danila.generated.types.ArtistPreview
import org.danila.generated.types.ArtistSimple
import org.danila.model.spotify.artist.Artist

fun Artist.toArtistPreview() = ArtistPreview(
    id = spotifyId,
    name = name,
    images = emptyList() // Images are loaded via ArtistImagesDataLoader
)

fun Artist.toArtistSimple() = ArtistSimple(
    id = spotifyId,
    name = name,
    followersTotal = followersTotal
)