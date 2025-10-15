package org.danila.web.controller.graphql.album

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.AlbumPreview
import org.danila.generated.types.AlbumPreviewPage
import org.danila.generated.types.OffsetPageInfo
import org.danila.model.spotify.album.AlbumType
import org.danila.services.model.album.AlbumService
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@DgsComponent
class AlbumGraphQLEndpoint(
    private val albumService: AlbumService
) {

    @DgsQuery
    fun topAlbumsByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        @InputArgument from: LocalDate?,
        @InputArgument to: LocalDate?,
        @InputArgument albumType: AlbumType,
        dfe: DgsDataFetchingEnvironment,
    ): AlbumPreviewPage {
        if (from != null && to != null) require(from <= to) { "From date must be before or equal to date" }

        val slice = albumService.findTopByPopularity(page = page, pageSize = size, from = from, to = to, albumType = albumType)
        val items = slice.content.map {
            AlbumPreview(id = it.getSpotifyId(), name = it.getName(), artists = emptyList(), covers = emptyList())
        }

        val needTotal = dfe.selectionSet.contains("totalCount")
        val total = if (needTotal) {
            val actualCount = albumService.countTopByPopularity(from = from, to = to, albumType = albumType)
            val cap = calculateLeaderboardCap(from, to)

            minOf(actualCount, cap.toLong())
        } else null

        return AlbumPreviewPage(
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
        if (from == null || to == null) return 500

        val daysBetween = ChronoUnit.DAYS.between(from, to)

        return when {
            daysBetween <= 31 -> 30
            daysBetween <= 366 -> 200
            else -> 500
        }
    }

}