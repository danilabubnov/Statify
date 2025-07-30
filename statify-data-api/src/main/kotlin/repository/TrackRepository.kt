package org.danila.repository

import org.danila.model.spotify.track.Track
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : JpaRepository<Track, String> {

    fun findByAlbum_ReleaseDate_AlbumReleaseYear(year: Int, pageable: Pageable): Page<Track>

    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Track>

}
