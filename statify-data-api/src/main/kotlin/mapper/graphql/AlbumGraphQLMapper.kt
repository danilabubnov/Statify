package org.danila.mapper.graphql

import org.danila.generated.types.AlbumDTO
import org.danila.generated.types.AlbumSimple
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumType

fun Album.toAlbumSimple() = AlbumSimple(
    id = spotifyId,
    name = name,
    albumType = albumType.toGeneratedAlbumType(),
    totalTracks = totalTracks,
    releaseDate = releaseDate.albumReleaseDateRaw
)

fun Album.toAlbumDTO() = AlbumDTO(
    id = spotifyId,
    name = name,
    albumType = albumType.toGeneratedAlbumType(),
    totalTracks = totalTracks,
    popularity = popularity,
    releaseDate = releaseDate.albumReleaseDateRaw,
    label = label
)

fun AlbumType.toGeneratedAlbumType(): org.danila.generated.types.AlbumType =
    if (this == AlbumType.ALBUM) org.danila.generated.types.AlbumType.ALBUM
    else if (this == AlbumType.SINGLE) org.danila.generated.types.AlbumType.SINGLE
    else if (this == AlbumType.COMPILATION) org.danila.generated.types.AlbumType.COMPILATION
    else throw IllegalStateException("AlbumType $this is not implemented for AlbumGraphQLMapper")