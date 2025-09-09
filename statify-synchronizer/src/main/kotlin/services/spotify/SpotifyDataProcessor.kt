package org.danila.services.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.danila.dto.spotify.*
import org.danila.dto.spotify.album.AlbumDTO
import org.danila.dto.spotify.album.SavedAlbumItemDTO
import org.danila.dto.spotify.artist.ArtistDTO
import org.danila.dto.spotify.track.SavedTrackItemDTO
import org.danila.dto.spotify.track.TrackDTO
import org.danila.model.spotify.AlbumArtist
import org.danila.model.spotify.TrackArtist
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumImage
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.model.spotify.artist.ArtistImage
import org.danila.model.spotify.artist.UserFollowedArtist
import org.danila.model.spotify.track.Track
import org.danila.model.spotify.track.UserFavoriteTrack
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class SpotifyDataProcessor {

    suspend fun processData(
        userId: UUID,
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<SavedTrackItemDTO>,
        albumDTOs: List<SavedAlbumItemDTO>,
        existingData: ExistingData
    ): ConcurrentSaveCollections {
        val saveCollections = ConcurrentSaveCollections()

        coroutineScope {
            val job1 = launch(Dispatchers.Default) {
                handleArtists(
                    artistDTOs = artistDTOs,
                    existingData = existingData,
                    saveCollections = saveCollections,
                    userFollowedArtistOf = { UserFollowedArtist(userId = userId, artistId = it.id) }
                )
            }

            val job2 = launch(Dispatchers.Default) {
                handleAlbums(
                    items = albumDTOs,
                    existingData = existingData,
                    saveCollections = saveCollections,
                    albumOf = { it.album },
                    userFavoriteOf = { UserFavoriteAlbum(userId = userId, albumId = it.album.id, addedAt = Instant.parse(it.addedAt)) }
                )
            }

            val job3 = launch(Dispatchers.Default) {
                handleTracks(
                    items = trackDTOs,
                    existingData = existingData,
                    saveCollections = saveCollections,
                    trackOf = { it.track },
                    userFavoriteOf = { UserFavoriteTrack(userId = userId, trackId = it.track.id, addedAt = Instant.parse(it.addedAt)) }
                )
            }

            joinAll(job1, job2, job3)
        }

        return saveCollections
    }

    suspend fun processData(
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<TrackDTO>,
        albumDTOs: List<AlbumDTO>,
        existingData: ExistingData
    ): ConcurrentSaveCollections {
        val saveCollections = ConcurrentSaveCollections()

        coroutineScope {
            val job1 = launch(Dispatchers.Default) {
                handleArtists(
                    artistDTOs = artistDTOs,
                    existingData = existingData,
                    saveCollections = saveCollections,
                    userFollowedArtistOf = { null }
                )
            }

            val job2 = launch(Dispatchers.Default) {
                handleAlbums(
                    items = albumDTOs,
                    existingData = existingData,
                    saveCollections = saveCollections,
                    albumOf = { it },
                    userFavoriteOf = { null }
                )
            }

            val job3 = launch(Dispatchers.Default) {
                handleTracks(
                    items = trackDTOs,
                    existingData = existingData,
                    saveCollections = saveCollections,
                    trackOf = { it },
                    userFavoriteOf = { null }
                )
            }

            joinAll(job1, job2, job3)
        }

        return saveCollections
    }

    private suspend fun handleArtists(
        artistDTOs: List<ArtistDTO>,
        existingData: ExistingData,
        saveCollections: ConcurrentSaveCollections,
        userFollowedArtistOf: (ArtistDTO) -> UserFollowedArtist?
    ) {
        artistDTOs.forEach { artistDTO ->
            val existingArtist = existingData.artists.find { it.spotifyId == artistDTO.id }

            if (existingArtist == null || existingArtist.isSimpleArtist() || !existingArtist.matchesDto(artistDTO)) {
                saveCollections.addArtistIfAbsent(artistDTO.toFullArtistDb())
            }

            val isUserFollowingArtist = existingData.userFollowedArtists.any { it.artistId == artistDTO.id }

            if (!isUserFollowingArtist) {
                val userFollowedArtist = userFollowedArtistOf(artistDTO)

                if (userFollowedArtist != null) saveCollections.addUserFollowedArtistIfAbsent(userFollowedArtist)
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

    private suspend fun <T> handleAlbums(
        items: List<T>,
        existingData: ExistingData,
        saveCollections: ConcurrentSaveCollections,
        albumOf: (T) -> AlbumDTO,
        userFavoriteOf: (T) -> UserFavoriteAlbum?
    ) {
        items.forEach { item ->
            val dto = albumOf(item)
            val existingAlbum = existingData.albums.find { it.spotifyId == dto.id }

            if (existingAlbum == null || existingAlbum.isSimpleAlbum() || !existingAlbum.matchesDto(dto)) {
                saveCollections.addAlbumIfAbsent(dto.toFullAlbumDb())
            }

            val userFavorite = userFavoriteOf(item)

            if (userFavorite != null) {
                val alreadyExists = existingData.userFavoriteAlbums.any { fav -> fav.albumId == userFavorite.albumId && fav.addedAt == userFavorite.addedAt }

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

    private suspend fun <T> handleTracks(
        items: List<T>,
        existingData: ExistingData,
        saveCollections: ConcurrentSaveCollections,
        trackOf: (T) -> TrackDTO,
        userFavoriteOf: (T) -> UserFavoriteTrack?
    ) {
        items.forEach { item ->
            val dto = trackOf(item)
            val existingTrack = existingData.tracks.find { it.spotifyId == dto.id }

            if (existingTrack == null || existingTrack.isSimpleTrack() || !existingTrack.matchesDto(dto)) {
                saveCollections.addTrackIfAbsent(dto.toFullTrackDb())
            }

            val userFavorite = userFavoriteOf(item)

            if (userFavorite != null) {
                val alreadyExists = existingData.userFavoriteTracks.any { fav -> fav.trackId == userFavorite.trackId && fav.addedAt == userFavorite.addedAt }

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

data class ConcurrentSaveCollections(
    val artists: MutableSet<Artist> = mutableSetOf(),
    val artistImages: MutableSet<ArtistImage> = mutableSetOf(),
    val artistGenres: MutableSet<ArtistGenre> = mutableSetOf(),
    val albums: MutableSet<Album> = mutableSetOf(),
    val albumImages: MutableSet<AlbumImage> = mutableSetOf(),
    val albumArtists: MutableSet<AlbumArtist> = mutableSetOf(),
    val tracks: MutableSet<Track> = mutableSetOf(),
    val trackArtists: MutableSet<TrackArtist> = mutableSetOf(),
    val userFollowedArtists: MutableSet<UserFollowedArtist> = mutableSetOf(),
    val userFavoriteTracks: MutableSet<UserFavoriteTrack> = mutableSetOf(),
    val userFavoriteAlbums: MutableSet<UserFavoriteAlbum> = mutableSetOf()
) {

    private val artistsMutex = Mutex()
    private val artistImagesMutex = Mutex()
    private val artistGenresMutex = Mutex()
    private val albumsMutex = Mutex()
    private val albumImagesMutex = Mutex()
    private val albumArtistsMutex = Mutex()
    private val tracksMutex = Mutex()
    private val trackArtistsMutex = Mutex()
    private val userFollowedArtistsMutex = Mutex()
    private val userFavoriteTracksMutex = Mutex()
    private val userFavoriteAlbumsMutex = Mutex()

    suspend fun addArtistIfAbsent(artist: Artist) {
        artistsMutex.withLock {
            if (this.artists.none { it.spotifyId == artist.spotifyId }) {
                this.artists.add(artist)
            } else if (this.artists.any { it.isSimpleArtist() && !artist.isSimpleArtist() }) {
                this.artists.removeIf { it.spotifyId == artist.spotifyId }
                this.artists.add(artist)
            }
        }
    }

    suspend fun addTrackIfAbsent(track: Track) {
        tracksMutex.withLock {
            if (this.tracks.none { it.spotifyId == track.spotifyId }) {
                this.tracks.add(track)
            } else if (this.tracks.any { it.isSimpleTrack() && !track.isSimpleTrack() }) {
                this.tracks.removeIf { it.spotifyId == track.spotifyId }
                this.tracks.add(track)
            }
        }
    }

    suspend fun addAlbumIfAbsent(album: Album) {
        albumsMutex.withLock {
            if (this.albums.none { it.spotifyId == album.spotifyId }) {
                this.albums.add(album)
            } else if (this.albums.any { it.isSimpleAlbum() && !album.isSimpleAlbum() }) {
                this.albums.removeIf { it.spotifyId == album.spotifyId }
                this.albums.add(album)
            }
        }
    }

    suspend fun addAlbumImageIfAbsent(albumImage: AlbumImage) {
        albumImagesMutex.withLock {
            if (this.albumImages.none { it.albumId == albumImage.albumId && it.imageUrl == albumImage.imageUrl }) {
                this.albumImages.add(albumImage)
            }
        }
    }

    suspend fun addArtistImageIfAbsent(artistImage: ArtistImage) {
        artistImagesMutex.withLock {
            if (this.artistImages.none { it.artistId == artistImage.artistId && it.imageUrl == artistImage.imageUrl }) {
                this.artistImages.add(artistImage)
            }
        }
    }

    suspend fun addArtistGenreIfAbsent(artistGenre: ArtistGenre) {
        artistGenresMutex.withLock {
            if (this.artistGenres.none { it.artistId == artistGenre.artistId && it.genre == artistGenre.genre }) {
                this.artistGenres.add(artistGenre)
            }
        }
    }

    suspend fun addAlbumArtistIfAbsent(albumArtist: AlbumArtist) {
        albumArtistsMutex.withLock {
            if (this.albumArtists.none { it.albumId == albumArtist.albumId && it.artistId == albumArtist.artistId }) {
                this.albumArtists.add(albumArtist)
            }
        }
    }

    suspend fun addTrackArtistIfAbsent(trackArtist: TrackArtist) {
        trackArtistsMutex.withLock {
            if (this.trackArtists.none { it.trackId == trackArtist.trackId && it.artistId == trackArtist.artistId }) {
                this.trackArtists.add(trackArtist)
            }
        }
    }

    suspend fun addUserFollowedArtistIfAbsent(userFavoriteArtist: UserFollowedArtist) {
        userFollowedArtistsMutex.withLock {
            if (this.userFollowedArtists.none { it.userId == userFavoriteArtist.userId && it.artistId == userFavoriteArtist.artistId }) {
                this.userFollowedArtists.add(userFavoriteArtist)
            }
        }
    }

    suspend fun addUserFavoriteTrackIfAbsent(userFavoriteTrack: UserFavoriteTrack) {
        userFavoriteTracksMutex.withLock {
            if (this.userFavoriteTracks.none { it.userId == userFavoriteTrack.userId && it.trackId == userFavoriteTrack.trackId }) {
                this.userFavoriteTracks.add(userFavoriteTrack)
            }
        }
    }

    suspend fun addUserFavoriteAlbumIfAbsent(userFavoriteAlbum: UserFavoriteAlbum) {
        userFavoriteAlbumsMutex.withLock {
            if (this.userFavoriteAlbums.none { it.userId == userFavoriteAlbum.userId && it.albumId == userFavoriteAlbum.albumId }) {
                this.userFavoriteAlbums.add(userFavoriteAlbum)
            }
        }
    }

}