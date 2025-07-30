package org.danila.services.model

import jakarta.persistence.EntityNotFoundException
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumType
import org.danila.model.spotify.track.Track
import org.danila.repository.AlbumRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlbumService(
    private val albumRepository: AlbumRepository,
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(
        page: Int,
        pageSize: Int,
        year: Int?,
        albumType: AlbumType
    ): List<Album> {
        val sortDescByPopularity = Sort.by(Sort.Direction.DESC, "popularity")
        val pageable = PageRequest.of(page, pageSize, sortDescByPopularity)

        return if (year != null) albumRepository.findByReleaseDate_AlbumReleaseYearAndAlbumType(year = year, albumType = albumType, pageable = pageable).content
        else albumRepository.findByAlbumType(albumType = albumType, pageable = pageable).content
    }

    @Transactional(readOnly = true)
    fun findById(id: String): Album = albumRepository.findById(id).orElseThrow { EntityNotFoundException("Album with id '$id' not found") }

    @Transactional(readOnly = true)
    fun findTracksByAlbum(id: String): List<Track> = findById(id).tracks.sortedBy { it.trackNumber }

    @Transactional(readOnly = true)
    fun searchByName(query: String, page: Int, pageSize: Int): List<Album> {
        val pageable = PageRequest.of(page, pageSize)

        return albumRepository.findByNameContainingIgnoreCase(name = query, pageable = pageable).content
    }

    @Transactional(readOnly = true)
    fun findByYear(year: Int): List<Album> = albumRepository.findByReleaseDate_AlbumReleaseYear(year = year)

}