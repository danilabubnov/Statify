package org.danila.web.controller.graphql.track

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.TrackPreview
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
    ): List<TrackPreview> {
        if (year != null) {
            val currentYear = Year.now().value

            require(year in 1900..currentYear) { "Year must be between 1900 and $currentYear" }
        }

        return trackService.findTopByPopularity(page = page, pageSize = size, year = year)
    }

}