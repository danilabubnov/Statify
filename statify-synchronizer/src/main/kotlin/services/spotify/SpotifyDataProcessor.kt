package org.danila.services.spotify

import org.danila.dto.*
import org.danila.dto.album.AlbumDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.TrackDTO
import org.danila.model.spotify.AlbumArtist
import org.danila.model.spotify.TrackArtist
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumImage
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.model.spotify.artist.ArtistImage
import org.danila.model.spotify.track.Track
import org.springframework.stereotype.Service

@Service
class SpotifyDataProcessor {

    fun processData(artistDTOs: List<ArtistDTO>, trackDTOs: List<TrackDTO>, albumDTOs: List<AlbumDTO>, existingData: ExistingData): SaveCollections {
        val saveCollections = SaveCollections()

        handleArtists(artistDTOs = artistDTOs, existingData = existingData, saveCollections = saveCollections)
        handleAlbums(albumDTOs = albumDTOs, existingData = existingData, saveCollections = saveCollections)
        handleTracks(trackDTOs = trackDTOs, existingData = existingData, saveCollections = saveCollections)

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

    private fun handleAlbums(albumDTOs: List<AlbumDTO>, existingData: ExistingData, saveCollections: SaveCollections) {
        albumDTOs.forEach { albumDTO ->
            val existingAlbum = existingData.albums.find { it.spotifyId == albumDTO.id }

            if (existingAlbum == null || existingAlbum.isSimpleAlbum() || !existingAlbum.matchesDto(albumDTO)) {
                saveCollections.addAlbumIfAbsent(albumDTO.toFullAlbumDb())
            }

            val existingAlbumImages = existingData.albumImages.filter { it.albumId == albumDTO.id }
            var albumImageIndex = existingAlbumImages.size

            albumDTO.images.forEach { imageDTO ->
                if (existingAlbumImages.none { it.albumId == albumDTO.id && it.imageUrl == imageDTO.url }) {
                    saveCollections.addAlbumImageIfAbsent(imageDTO.toAlbumImageDb(index = albumImageIndex++, albumId = albumDTO.id))
                }

                // TODO: remove missing albumImages from db
            }

            albumDTO.artists.forEach { artistSimpleDto ->
                if (existingData.artists.none { it.spotifyId == artistSimpleDto.id }) {
                    saveCollections.addArtistIfAbsent(artistSimpleDto.toSimpleArtistDb())
                }

                if (existingData.albumArtists.none { it.artistId == artistSimpleDto.id && it.albumId == albumDTO.id }) {
                    saveCollections.addAlbumArtistIfAbsent(AlbumArtist(artistId = artistSimpleDto.id, albumId = albumDTO.id))

                    // TODO: remove missing albumArtists from db
                }
            }

            albumDTO.tracks.items.forEach { trackSimpleDto ->
                if (existingData.tracks.none { it.spotifyId == trackSimpleDto.id }) {
                    saveCollections.addTrackIfAbsent(trackSimpleDto.toSimpleTrackDb(albumId = albumDTO.id))
                }

                trackSimpleDto.artists.forEach { artistSimpleDto ->
                    if (existingData.artists.none { it.spotifyId == artistSimpleDto.id }) {
                        saveCollections.addArtistIfAbsent(artistSimpleDto.toSimpleArtistDb())
                    }

                    if (existingData.trackArtists.none { it.trackId == trackSimpleDto.id && it.artistId == artistSimpleDto.id }) {
                        saveCollections.addTrackArtistIfAbsent(TrackArtist(trackId = trackSimpleDto.id, artistId = artistSimpleDto.id))
                    }

                    // TODO: remove missing trackArtists from db
                }
            }
        }
    }

    private fun handleTracks(trackDTOs: List<TrackDTO>, existingData: ExistingData, saveCollections: SaveCollections) {
        trackDTOs.forEach { trackDTO ->
            val existingTrack = existingData.tracks.find { it.spotifyId == trackDTO.id }

            if (existingTrack == null || existingTrack.isSimpleTrack() || !existingTrack.matchesDto(trackDTO)) {
                saveCollections.addTrackIfAbsent(trackDTO.toFullTrackDb())
            }

            if (existingData.albums.none { it.spotifyId == trackDTO.album.id }) {
                saveCollections.addAlbumIfAbsent(trackDTO.album.toSimpleAlbumDb())
            }

            val existingAlbumImages = existingData.albumImages.filter { it.albumId == trackDTO.album.id }
            var albumImageIndex = existingAlbumImages.size

            trackDTO.album.images.forEach { imageDTO ->
                if (existingAlbumImages.none { it.albumId == trackDTO.album.id && it.imageUrl == imageDTO.url }) {
                    saveCollections.addAlbumImageIfAbsent(imageDTO.toAlbumImageDb(index = albumImageIndex++, albumId = trackDTO.album.id))
                }

                // TODO: remove missing images from db
            }

            trackDTO.album.artists.forEach { artistSimpleDto ->
                if (existingData.artists.none { it.spotifyId == artistSimpleDto.id }) {
                    saveCollections.addArtistIfAbsent(artistSimpleDto.toSimpleArtistDb())
                }

                if (existingData.albumArtists.none { it.albumId == trackDTO.album.id && it.artistId == artistSimpleDto.id }) {
                    saveCollections.addAlbumArtistIfAbsent(AlbumArtist(albumId = trackDTO.album.id, artistId = artistSimpleDto.id))

                    // TODO: remove missing albumArtists from db
                }
            }

            trackDTO.artists.forEach { artistSimpleDto ->
                if (existingData.artists.none { it.spotifyId == artistSimpleDto.id }) {
                    saveCollections.addArtistIfAbsent(artistSimpleDto.toSimpleArtistDb())
                }

                if (existingData.trackArtists.none { it.trackId == trackDTO.id && it.artistId == artistSimpleDto.id }) {
                    saveCollections.addTrackArtistIfAbsent(TrackArtist(trackId = trackDTO.id, artistId = artistSimpleDto.id))

                    // TODO: remove missing trackArtists from db
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
    val trackArtists: MutableSet<TrackArtist> = mutableSetOf()
) {

    fun addArtistIfAbsent(artist: Artist) {
        if (this.artists.none { it.spotifyId == artist.spotifyId }) {
            this.artists.add(artist)
        }
        else if (this.artists.any { it.isSimpleArtist() && !artist.isSimpleArtist() }) {
            this.artists.removeIf { it.spotifyId == artist.spotifyId }
            this.artists.add(artist)
        }
    }

    fun addTrackIfAbsent(track: Track) {
        if (this.tracks.none { it.spotifyId == track.spotifyId }) {
            this.tracks.add(track)
        }
        else if (this.tracks.any { it.isSimpleTrack() && !track.isSimpleTrack() }) {
            this.tracks.removeIf { it.spotifyId == track.spotifyId }
            this.tracks.add(track)
        }
    }

    fun addAlbumIfAbsent(album: Album) {
        if (this.albums.none { it.spotifyId == album.spotifyId }) {
            this.albums.add(album)
        }
        else if (this.albums.any { it.isSimpleAlbum() && !album.isSimpleAlbum() }) {
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

}