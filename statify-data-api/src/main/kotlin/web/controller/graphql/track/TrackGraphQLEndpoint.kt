package org.danila.web.controller.graphql.track

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.TrackDTO
import org.danila.generated.types.TrackPreview
import org.danila.generated.types.TrackSimple
import org.danila.mapper.graphql.toTrackDTO
import org.danila.mapper.graphql.toTrackPreview
import org.danila.mapper.graphql.toTrackSimple
import org.danila.services.model.track.TrackService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@DgsComponent
class TrackGraphQLEndpoint(
    private val trackService: TrackService
) {

    @DgsQuery
    fun topTracksByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        @InputArgument year: Int?,
    ): List<TrackPreview> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularity"))

        return trackService.findTopByPopularity(pageable = pageable, year = year)
            .map { it.toTrackPreview() }
    }

    @DgsQuery
    fun track(@InputArgument id: String): TrackDTO =
        trackService.findById(id).toTrackDTO()

    @DgsQuery
    fun searchTracks(
        @InputArgument query: String,
        @InputArgument page: Int,
        @InputArgument size: Int
    ): List<TrackSimple> {
        val sanitized = query.trim().takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Query cannot be empty")
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularity"))

        return trackService.searchByName(query = sanitized, pageable = pageable)
            .map { it.toTrackSimple() }
    }

}