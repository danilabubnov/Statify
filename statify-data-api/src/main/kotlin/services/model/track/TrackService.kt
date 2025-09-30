package org.danila.services.model.track

import org.danila.generated.types.TrackPreview
import org.danila.repository.TrackRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TrackService(
    private val trackRepository: TrackRepository
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(
        page: Int,
        pageSize: Int,
        year: Int?
    ): List<TrackPreview> {
        val pageable = PageRequest.of(page, pageSize)
        val topTrackIdsByPopularity = trackRepository.findTopTracksByPopularity(year = year, pageable = pageable).content

        return topTrackIdsByPopularity.map { TrackPreview(id = it.getSpotifyId(), name = it.getName(), artists = emptyList(), covers = emptyList()) }
    }

    @Transactional(readOnly = true)
    fun countTopByPopularity(year: Int?): Long {
        return trackRepository.countTopTracksByPopularity(year)
    }

}