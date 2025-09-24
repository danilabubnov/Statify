package org.danila.services.model.artist

import org.danila.generated.types.ArtistPreview
import org.danila.repository.ArtistRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArtistService(
    private val artistRepository: ArtistRepository,
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(
        page: Int,
        pageSize: Int,
    ): List<ArtistPreview> {
        val pageable = PageRequest.of(page, pageSize)
        val topArtistIdsByPopularity = artistRepository.findAllByOrderByPopularityDesc(pageable).content

        return topArtistIdsByPopularity.map { artist -> ArtistPreview(id = artist.spotifyId, name = artist.name, images = emptyList()) }
    }

}