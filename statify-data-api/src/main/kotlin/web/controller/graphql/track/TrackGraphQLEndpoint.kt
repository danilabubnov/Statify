package org.danila.web.controller.graphql.track

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.OffsetPageInfo
import org.danila.generated.types.TrackPreview
import org.danila.generated.types.TrackPreviewPage
import org.danila.services.model.track.TrackService
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@DgsComponent
class TrackGraphQLEndpoint(
    private val trackService: TrackService
) {

    @DgsQuery
    fun topTracksByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        @InputArgument from: LocalDate?,
        @InputArgument to: LocalDate?,
        dfe: DgsDataFetchingEnvironment,
    ): TrackPreviewPage {
        if (from != null && to != null) require(from <= to) { "From date must be before or equal to date" }

        val slice = trackService.findTopByPopularity(page = page, pageSize = size, from = from, to = to)
        val items = slice.content.map {
            TrackPreview(id = it.getSpotifyId(), name = it.getName(), artists = emptyList(), covers = emptyList())
        }

        val needTotal = dfe.selectionSet.contains("totalCount")
        val total = if (needTotal) {
            val actualCount = trackService.countTopByPopularity(from = from, to = to)
            val cap = calculateLeaderboardCap(from, to)

            minOf(actualCount, cap.toLong())
        } else null

        return TrackPreviewPage(
            items = items,
            pageInfo = OffsetPageInfo(
                hasNextPage = slice.hasNext(),
                hasPreviousPage = page > 0,
                page = page,
                size = size,
            ),
            totalCount = total
        )
    }

    private fun calculateLeaderboardCap(from: LocalDate?, to: LocalDate?): Int {
        if (from == null || to == null) return 1000

        val daysBetween = ChronoUnit.DAYS.between(from, to)

        return when {
            daysBetween <= 31 -> 100
            daysBetween <= 366 -> 500
            else -> 1000
        }
    }

}