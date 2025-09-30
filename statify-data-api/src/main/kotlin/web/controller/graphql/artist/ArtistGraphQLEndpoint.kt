package org.danila.web.controller.graphql.artist

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.ArtistPreviewPage
import org.danila.generated.types.OffsetPageInfo
import org.danila.services.model.artist.ArtistService

@DgsComponent
class ArtistGraphQLEndpoint(
    private val artistService: ArtistService
) {

    @DgsQuery
    fun topArtistsByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        dfe: DgsDataFetchingEnvironment,
    ): ArtistPreviewPage {
        val itemsPlusOne = artistService.findTopByPopularity(page = page, pageSize = size)

        val hasNext = itemsPlusOne.size > size
        val items = itemsPlusOne.take(size)

        val needTotal = dfe.selectionSet.contains("totalCount")

        val total = if (needTotal) artistService.countTopByPopularity() else null

        return ArtistPreviewPage(
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