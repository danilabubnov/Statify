package org.danila.dto

import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.AlbumSimpleDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.artist.ArtistSimpleDTO
import org.danila.dto.common.ImageDTO
import org.danila.dto.track.TrackDTO
import org.danila.dto.track.TrackItemDTO
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumImage
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.model.spotify.artist.ArtistImage
import org.danila.model.spotify.track.Track

fun ArtistDTO.toFullArtistDb() = Artist(
    spotifyId = this.id,
    name = this.name,
    followersTotal = this.followers.total,
    popularity = this.popularity
)

fun ImageDTO.toArtistImageDb(index: Int, artistId: String) = ArtistImage(
    imageOrder = index,
    artistId = artistId,
    imageUrl = this.url,
    imageHeight = this.height,
    imageWidth = this.width
)

fun String.toArtistGenreDb(artistId: String) = ArtistGenre(
    artistId = artistId,
    genre = this
)

fun ArtistSimpleDTO.toSimpleArtistDb() = Artist(
    spotifyId = this.id,
    name = this.name,
    followersTotal = null,
    popularity = null
)

fun TrackItemDTO.toSimpleTrackDb(albumId: String) = Track(
    spotifyId = this.id,
    name = this.name,
    durationMs = this.durationMs,
    explicit = this.explicit,
    popularity = null,
    trackNumber = this.trackNumber,
    albumId = albumId
)

fun TrackDTO.toFullTrackDb() = Track(
    spotifyId = this.id,
    name = this.name,
    durationMs = this.durationMs,
    explicit = this.explicit,
    popularity = this.popularity,
    trackNumber = this.trackNumber,
    albumId = this.album.id
)

fun AlbumSimpleDTO.toSimpleAlbumDb() = Album(
    spotifyId = this.id,
    albumType = this.albumType,
    totalTracks = this.totalTracks,
    name = this.name,
    label = null,
    popularity = null,
    releaseDateRaw = this.releaseDate,
    releaseDatePrecision = parsePrecision(this.releaseDate),
    releaseYear = parseYear(this.releaseDate),
    releaseMonth = parseMonth(this.releaseDate),
    releaseDay = parseDay(this.releaseDate)
)

fun AlbumDTO.toFullAlbumDb() = Album(
    spotifyId = this.id,
    albumType = this.albumType,
    totalTracks = this.totalTracks,
    name = this.name,
    label = this.label,
    popularity = this.popularity,
    releaseDateRaw = this.releaseDate,
    releaseDatePrecision = parsePrecision(this.releaseDate),
    releaseYear = parseYear(this.releaseDate),
    releaseMonth = parseMonth(this.releaseDate),
    releaseDay = parseDay(this.releaseDate)
)

fun ImageDTO.toAlbumImageDb(index: Int, albumId: String) = AlbumImage(
    imageOrder = index,
    albumId = albumId,
    imageUrl = this.url,
    imageHeight = this.height,
    imageWidth = this.width
)

private fun parseYear(date: String) = date.substring(0, 4).toInt()
private fun parseMonth(date: String) = date.split("-").getOrNull(1)?.toInt()
private fun parseDay(date: String) = date.split("-").getOrNull(2)?.toInt()
private fun parsePrecision(releaseDate: String): String {
    return when {
        releaseDate.contains("-") -> {
            when (releaseDate.count { it == '-' }) {
                1 -> "MONTH"
                2 -> "DAY"
                else -> throw IllegalArgumentException("Некорректный формат даты")
            }
        }

        else -> "YEAR"
    }
}