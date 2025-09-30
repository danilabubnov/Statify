package org.danila.services.model.album

import org.danila.generated.types.AlbumPreview
import org.danila.model.spotify.album.AlbumType
import org.danila.repository.AlbumRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlbumService(
    private val albumRepository: AlbumRepository
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(
        page: Int,
        pageSize: Int,
        year: Int?,
        albumType: AlbumType
    ): List<AlbumPreview> {
        val pageable = PageRequest.of(page, pageSize)
        val topAlbumIdsByPopularity = albumRepository.findTopAlbumsByPopularity(year = year, albumType = albumType, pageable = pageable).content

        return topAlbumIdsByPopularity.map { AlbumPreview(id = it.getSpotifyId(), name = it.getName(), artists = emptyList(), covers = emptyList()) }
    }

    @Transactional(readOnly = true)
    fun countTopByPopularity(
        year: Int?,
        albumType: AlbumType
    ): Long {
        return albumRepository.countTopAlbumsByPopularity(year = year, albumType = albumType)
    }

}