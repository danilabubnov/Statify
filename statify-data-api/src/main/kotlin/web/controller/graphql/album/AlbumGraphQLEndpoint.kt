package org.danila.web.controller.graphql.album

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.AlbumPreviewPage
import org.danila.generated.types.OffsetPageInfo
import org.danila.model.spotify.album.AlbumType
import org.danila.services.model.album.AlbumService
import java.time.Year

@DgsComponent
class AlbumGraphQLEndpoint(
    private val albumService: AlbumService
) {

    @DgsQuery
    fun topAlbumsByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        @InputArgument year: Int?,
        @InputArgument albumType: AlbumType,
        dfe: DgsDataFetchingEnvironment,
    ): AlbumPreviewPage {
        if (year != null) {
            val currentYear = Year.now().value

            require(year in 1900..currentYear) { "Year must be between 1900 and $currentYear" }
        }

        val itemsPlusOne = albumService.findTopByPopularity(page = page, pageSize = size + 1, year = year, albumType = albumType)

        val hasNext = itemsPlusOne.size > size
        val items = itemsPlusOne.take(size)

        val needTotal = dfe.selectionSet.contains("totalCount")

        val total = if (needTotal) albumService.countTopByPopularity(year, albumType) else null

        return AlbumPreviewPage(
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