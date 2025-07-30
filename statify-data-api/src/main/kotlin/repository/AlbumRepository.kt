package org.danila.repository

import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AlbumRepository : JpaRepository<Album, String> {

    fun findByAlbumType(albumType: AlbumType, pageable: Pageable): Page<Album>

    fun findByReleaseDate_AlbumReleaseYearAndAlbumType(year: Int, albumType: AlbumType, pageable: Pageable): Page<Album>

    fun findByReleaseDate_AlbumReleaseYear(year: Int): List<Album>

    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Album>

}