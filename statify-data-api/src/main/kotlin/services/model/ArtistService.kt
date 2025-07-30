package org.danila.services.model

import jakarta.persistence.EntityNotFoundException
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.track.Track
import org.danila.repository.AlbumRepository
import org.danila.repository.ArtistRepository
import org.danila.repository.TrackRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArtistService(
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
    private val albumRepository: AlbumRepository
) {

    @Transactional(readOnly = true)
    fun findTop(pageable: Pageable): List<Artist> = artistRepository.findAll(pageable).content

    @Transactional(readOnly = true)
    fun findById(id: String): Artist = artistRepository.findById(id).orElseThrow { EntityNotFoundException("Artist with id '$id' not found") }

    @Transactional(readOnly = true)
    fun findAlbumsByArtist(id: String): List<Album> = albumRepository.findAll(PageRequest.of(0, Int.MAX_VALUE, Sort.by("releaseDate.albumReleaseYear").descending())).content
        .filter { artist -> artist.artists.any { it.spotifyId == id } }

    @Transactional(readOnly = true)
    fun findTopTracksByArtist(id: String): List<Track> {
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "popularity"))

        return trackRepository.findAll(pageable).content.filter { t -> t.artists.any { it.spotifyId == id } }
    }

}