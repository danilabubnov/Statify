package org.danila.web.controller.graphql.album

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.AlbumPreview
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
        @InputArgument albumType: AlbumType
    ): List<AlbumPreview> {
        if (year != null) {
            val currentYear = Year.now().value

            require(year in 1900..currentYear) { "Year must be between 1900 and $currentYear" }
        }

        return albumService.findTopByPopularity(page = page, pageSize = size, year = year, albumType = albumType)
    }

}