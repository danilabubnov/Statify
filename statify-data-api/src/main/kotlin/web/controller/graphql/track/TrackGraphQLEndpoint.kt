package org.danila.web.controller.graphql.track

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.OffsetPageInfo
import org.danila.generated.types.TrackPreviewPage
import org.danila.services.model.track.TrackService
import java.time.Year

@DgsComponent
class TrackGraphQLEndpoint(
    private val trackService: TrackService
) {

    @DgsQuery
    fun topTracksByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        @InputArgument year: Int?,
        dfe: DgsDataFetchingEnvironment,
    ): TrackPreviewPage {
        if (year != null) {
            val currentYear = Year.now().value

            require(year in 1900..currentYear) { "Year must be between 1900 and $currentYear" }
        }

        val itemsPlusOne = trackService.findTopByPopularity(page = page, pageSize = size + 1, year = year)

        val hasNext = itemsPlusOne.size > size
        val items = itemsPlusOne.take(size)

        val needTotal = dfe.selectionSet.contains("totalCount")

        val total = if (needTotal) trackService.countTopByPopularity(year = year) else null

        return TrackPreviewPage(
            items = items,
            pageInfo = OffsetPageInfo(
                hasNextPage = hasNext,
                hasPreviousPage = page > 0,
                page = page,
                size = size,
            ),
            totalCount = total
        )
    }

}