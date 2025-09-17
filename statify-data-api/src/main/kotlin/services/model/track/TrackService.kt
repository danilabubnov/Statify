package org.danila.services.model.track

import jakarta.persistence.EntityNotFoundException
import org.danila.model.spotify.track.Track
import org.danila.repository.TrackRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TrackService(
    private val trackRepository: TrackRepository
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(pageable: Pageable, year: Int?): List<Track> =
        if (year != null) trackRepository.findByAlbum_ReleaseDate_AlbumReleaseYear(year, pageable).content
        else trackRepository.findAll(pageable).content

    @Transactional(readOnly = true)
    fun findById(id: String): Track = trackRepository.findById(id).orElseThrow { EntityNotFoundException("Track with id '$id' not found") }

    @Transactional(readOnly = true)
    fun searchByName(query: String, pageable: Pageable): List<Track> = trackRepository.findByNameContainingIgnoreCase(query, pageable).content

}