package org.danila.mapper.graphql

import org.danila.generated.types.AlbumPreview
import org.danila.model.spotify.Image
import org.danila.model.spotify.album.Album

fun Album.toAlbumPreview() = AlbumPreview(
    id = spotifyId,
    name = name,
    artists = artists.map { it.toArtistSimple() },
    covers = images.map { it.toImage() }
)

fun Image.toImage() = org.danila.generated.types.Image(
    imageUrl = imageUrl,
    imageWidth = imageWidth,
    imageHeight = imageHeight
)