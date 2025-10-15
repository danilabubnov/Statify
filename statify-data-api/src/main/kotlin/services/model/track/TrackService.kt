package org.danila.services.model.track

import org.danila.repository.TrackRepository
import org.danila.repository.projection.TrackIdProjection
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TrackService(
    private val trackRepository: TrackRepository
) {

    @Transactional(readOnly = true)
    fun findTopByPopularity(page: Int, pageSize: Int, from: LocalDate?, to: LocalDate?): Slice<TrackIdProjection> {
        val pageable = PageRequest.of(page, pageSize)
        return trackRepository.findTopTracksByPopularity(from = from, to = to, pageable = pageable)
    }

    @Transactional(readOnly = true)
    fun countTopByPopularity(from: LocalDate?, to: LocalDate?): Long {
        return trackRepository.countTopTracksByPopularity(from = from, to = to)
    }

}