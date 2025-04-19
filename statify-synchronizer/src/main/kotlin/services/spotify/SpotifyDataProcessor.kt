package org.danila.services.spotify

import org.danila.dto.*
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.danila.model.spotify.AlbumArtist
import org.danila.model.spotify.TrackArtist
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumImage
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.model.spotify.artist.ArtistImage
import org.danila.model.spotify.track.Track
import org.danila.model.spotify.track.UserFavoriteTrack
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class SpotifyDataProcessor {

    fun processData(
        userId: UUID,
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<SavedTrackItemDTO>,
        albumDTOs: List<SavedAlbumItemDTO>,
        existingData: ExistingData
    ): SaveCollections {
        val saveCollections = SaveCollections()

        handleArtists(
            artistDTOs = artistDTOs,
            existingData = existingData,
            saveCollections = saveCollections
        )
        handleAlbums(
            items = albumDTOs,
            existingData = existingData,
            saveCollections = saveCollections,
            albumOf = { it.album },
            userFavoriteOf = { UserFavoriteAlbum(userId, it.album.id, Instant.parse(it.addedAt)) }
        )
        handleTracks(
            items = trackDTOs,
            existingData = existingData,
            saveCollections = saveCollections,
            trackOf = { it.track },
            userFavoriteOf = { UserFavoriteTrack(userId, it.track.id, Instant.parse(it.addedAt)) }
        )

        return saveCollections
    }

    fun processData(
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<TrackDTO>,
        albumDTOs: List<AlbumDTO>,
        existingData: ExistingData
    ): SaveCollections {
        val saveCollections = SaveCollections()

        handleArtists(
            artistDTOs = artistDTOs,
            existingData = existingData,
            saveCollections = saveCollections
        )
        handleAlbums(
            items = albumDTOs,
            existingData = existingData,
            saveCollections = saveCollections,
            albumOf = { it },
            userFavoriteOf = { null }
        )
        handleTracks(
            items = trackDTOs,
            existingData = existingData,
            saveCollections = saveCollections,
            trackOf = { it },
            userFavoriteOf = { null }
        )

        return saveCollections
    }

    private fun handleArtists(artistDTOs: List<ArtistDTO>, existingData: ExistingData, saveCollections: SaveCollections) {
        artistDTOs.forEach { artistDTO ->
            val existingArtist = existingData.artists.find { it.spotifyId == artistDTO.id }

            if (existingArtist == null || existingArtist.isSimpleArtist() || !existingArtist.matchesDto(artistDTO)) {
                saveCollections.addArtistIfAbsent(artistDTO.toFullArtistDb())
            }

            val existingArtistImages = existingData.artistImages.filter { it.artistId == artistDTO.id }
            var artistImageIndex = existingArtistImages.size

            artistDTO.images.forEach { imageDTO ->
                if (existingArtistImages.none { it.imageUrl == imageDTO.url }) {
                    saveCollections.addArtistImageIfAbsent(imageDTO.toArtistImageDb(index = artistImageIndex++, artistId = artistDTO.id))
                }

                // TODO: remove missing artistImages from db
            }

            val existingArtistGenres = existingData.artistGenres.filter { it.artistId == artistDTO.id }

            artistDTO.genres.forEach { genre ->
                if (existingArtistGenres.none { it.genre == genre }) {
                    saveCollections.addArtistGenreIfAbsent(genre.toArtistGenreDb(artistId = artistDTO.id))
                }

                // TODO: remove missing artistGenres from db
            }
        }
    }

    private inline fun <T> handleAlbums(
        items: List<T>,
        existingData: ExistingData,
        saveCollections: SaveCollections,
        crossinline albumOf: (T) -> AlbumDTO,
        crossinline userFavoriteOf: (T) -> UserFavoriteAlbum?
    ) {
        items.forEach { item ->
            val dto = albumOf(item)
            val existingAlbum = existingData.albums.find { it.spotifyId == dto.id }

            if (existingAlbum == null || existingAlbum.isSimpleAlbum() || !existingAlbum.matchesDto(dto)) {
                saveCollections.addAlbumIfAbsent(dto.toFullAlbumDb())
            }

            val userFavorite = userFavoriteOf(item)

            if (userFavorite != null) {
                val alreadyExists = existingData.userFavoriteAlbums.any { fav -> fav.albumId == dto.id }

                if (!alreadyExists) {
                    saveCollections.addUserFavoriteAlbumIfAbsent(userFavorite)
                }
            }

            val images = existingData.albumImages.filter { it.albumId == dto.id }
            var idx = images.size

            dto.images.forEach { image ->
                if (images.none { it.imageUrl == image.url }) {
                    saveCollections.addAlbumImageIfAbsent(image.toAlbumImageDb(index = idx++, albumId = dto.id))
                }
            }

            dto.artists.forEach { artist ->
                if (existingData.artists.none { it.spotifyId == artist.id }) {
                    saveCollections.addArtistIfAbsent(artist.toSimpleArtistDb())
                }

                if (existingData.albumArtists.none { it.albumId == dto.id && it.artistId == artist.id }) {
                    saveCollections.addAlbumArtistIfAbsent(AlbumArtist(albumId = dto.id, artistId = artist.id))
                }
            }

            dto.tracks.items.forEach { track ->
                if (existingData.tracks.none { it.spotifyId == track.id }) {
                    saveCollections.addTrackIfAbsent(track.toSimpleTrackDb(albumId = dto.id))
                }

                track.artists.forEach { artist ->
                    if (existingData.artists.none { it.spotifyId == artist.id }) {
                        saveCollections.addArtistIfAbsent(artist.toSimpleArtistDb())
                    }

                    if (existingData.trackArtists.none { it.trackId == track.id && it.artistId == artist.id }) {
                        saveCollections.addTrackArtistIfAbsent(TrackArtist(trackId = track.id, artistId = artist.id))
                    }
                }
            }
        }
    }

    private inline fun <T> handleTracks(
        items: List<T>,
        existingData: ExistingData,
        saveCollections: SaveCollections,
        crossinline trackOf: (T) -> TrackDTO,
        crossinline userFavoriteOf: (T) -> UserFavoriteTrack?
    ) {
        items.forEach { item ->
            val dto = trackOf(item)
            val existingTrack = existingData.tracks.find { it.spotifyId == dto.id }

            if (existingTrack == null || existingTrack.isSimpleTrack() || !existingTrack.matchesDto(dto)) {
                saveCollections.addTrackIfAbsent(dto.toFullTrackDb())
            }

            val userFavorite = userFavoriteOf(item)

            if (userFavorite != null) {
                val alreadyExists = existingData.userFavoriteTracks.any { fav -> fav.trackId == dto.id }

                if (!alreadyExists) {
                    saveCollections.addUserFavoriteTrackIfAbsent(userFavorite)
                }
            }

            if (existingData.albums.none { it.spotifyId == dto.album.id }) {
                saveCollections.addAlbumIfAbsent(dto.album.toSimpleAlbumDb())
            }

            val images = existingData.albumImages.filter { it.albumId == dto.album.id }
            var idx = images.size

            dto.album.images.forEach { image ->
                if (images.none { it.imageUrl == image.url }) {
                    saveCollections.addAlbumImageIfAbsent(image.toAlbumImageDb(index = idx++, albumId = dto.album.id))
                }
            }

            dto.album.artists.forEach { artist ->
                if (existingData.artists.none { it.spotifyId == artist.id }) {
                    saveCollections.addArtistIfAbsent(artist.toSimpleArtistDb())
                }

                if (existingData.albumArtists.none { it.albumId == dto.album.id && it.artistId == artist.id }) {
                    saveCollections.addAlbumArtistIfAbsent(AlbumArtist(albumId = dto.album.id, artistId = artist.id))
                }
            }

            dto.artists.forEach { artist ->
                if (existingData.artists.none { it.spotifyId == artist.id }) {
                    saveCollections.addArtistIfAbsent(artist.toSimpleArtistDb())
                }

                if (existingData.trackArtists.none { it.trackId == dto.id && it.artistId == artist.id }) {
                    saveCollections.addTrackArtistIfAbsent(TrackArtist(trackId = dto.id, artistId = artist.id))
                }
            }
        }
    }

}

data class SaveCollections(
    val artists: MutableSet<Artist> = mutableSetOf(),
    val artistImages: MutableSet<ArtistImage> = mutableSetOf(),
    val artistGenres: MutableSet<ArtistGenre> = mutableSetOf(),
    val albums: MutableSet<Album> = mutableSetOf(),
    val albumImages: MutableSet<AlbumImage> = mutableSetOf(),
    val albumArtists: MutableSet<AlbumArtist> = mutableSetOf(),
    val tracks: MutableSet<Track> = mutableSetOf(),
    val trackArtists: MutableSet<TrackArtist> = mutableSetOf(),
    val userFavoriteTracks: MutableSet<UserFavoriteTrack> = mutableSetOf(),
    val userFavoriteAlbums: MutableSet<UserFavoriteAlbum> = mutableSetOf()
) {

    fun addArtistIfAbsent(artist: Artist) {
        if (this.artists.none { it.spotifyId == artist.spotifyId }) {
            this.artists.add(artist)
        } else if (this.artists.any { it.isSimpleArtist() && !artist.isSimpleArtist() }) {
            this.artists.removeIf { it.spotifyId == artist.spotifyId }
            this.artists.add(artist)
        }
    }

    fun addTrackIfAbsent(track: Track) {
        if (this.tracks.none { it.spotifyId == track.spotifyId }) {
            this.tracks.add(track)
        } else if (this.tracks.any { it.isSimpleTrack() && !track.isSimpleTrack() }) {
            this.tracks.removeIf { it.spotifyId == track.spotifyId }
            this.tracks.add(track)
        }
    }

    fun addAlbumIfAbsent(album: Album) {
        if (this.albums.none { it.spotifyId == album.spotifyId }) {
            this.albums.add(album)
        } else if (this.albums.any { it.isSimpleAlbum() && !album.isSimpleAlbum() }) {
            this.albums.removeIf { it.spotifyId == album.spotifyId }
            this.albums.add(album)
        }
    }

    fun addAlbumImageIfAbsent(albumImage: AlbumImage) {
        if (this.albumImages.none { it.albumId == albumImage.albumId && it.imageUrl == albumImage.imageUrl }) {
            this.albumImages.add(albumImage)
        }
    }

    fun addArtistImageIfAbsent(artistImage: ArtistImage) {
        if (this.artistImages.none { it.artistId == artistImage.artistId && it.imageUrl == artistImage.imageUrl }) {
            this.artistImages.add(artistImage)
        }
    }

    fun addArtistGenreIfAbsent(artistGenre: ArtistGenre) {
        if (this.artistGenres.none { it.artistId == artistGenre.artistId && it.genre == artistGenre.genre }) {
            this.artistGenres.add(artistGenre)
        }
    }

    fun addAlbumArtistIfAbsent(albumArtist: AlbumArtist) {
        if (this.albumArtists.none { it.albumId == albumArtist.albumId && it.artistId == albumArtist.artistId }) {
            this.albumArtists.add(albumArtist)
        }
    }

    fun addTrackArtistIfAbsent(trackArtist: TrackArtist) {
        if (this.trackArtists.none { it.trackId == trackArtist.trackId && it.artistId == trackArtist.artistId }) {
            this.trackArtists.add(trackArtist)
        }
    }

    fun addUserFavoriteTrackIfAbsent(userFavoriteTrack: UserFavoriteTrack) {
        if (this.userFavoriteTracks.none { it.userId == userFavoriteTrack.userId && it.trackId == userFavoriteTrack.trackId }) {
            this.userFavoriteTracks.add(userFavoriteTrack)
        }
    }

    fun addUserFavoriteAlbumIfAbsent(userFavoriteAlbum: UserFavoriteAlbum) {
        if (this.userFavoriteAlbums.none { it.userId == userFavoriteAlbum.userId && it.albumId == userFavoriteAlbum.albumId }) {
            this.userFavoriteAlbums.add(userFavoriteAlbum)
        }
    }

}