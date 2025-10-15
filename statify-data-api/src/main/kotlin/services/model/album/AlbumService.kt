package org.danila.services.model.album

import org.danila.model.spotify.album.AlbumType
import org.danila.repository.AlbumRepository
import org.danila.repository.projection.AlbumIdProjection
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AlbumService(
    private val albumRepository: AlbumRepository
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(
        page: Int,
        pageSize: Int,
        from: LocalDate?,
        to: LocalDate?,
        albumType: AlbumType
    ): Slice<AlbumIdProjection> {
        val pageable = PageRequest.of(page, pageSize)
        return albumRepository.findTopAlbumsByPopularity(from = from, to = to, albumType = albumType, pageable = pageable)
    }

    @Transactional(readOnly = true)
    fun countTopByPopularity(
        from: LocalDate?,
        to: LocalDate?,
        albumType: AlbumType
    ): Long {
        return albumRepository.countTopAlbumsByPopularity(from = from, to = to, albumType = albumType)
    }

}