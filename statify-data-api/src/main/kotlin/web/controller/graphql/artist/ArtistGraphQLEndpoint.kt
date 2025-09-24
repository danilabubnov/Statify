package org.danila.web.controller.graphql.artist

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.ArtistPreview
import org.danila.services.model.artist.ArtistService

@DgsComponent
class ArtistGraphQLEndpoint(
    private val artistService: ArtistService
) {

    @DgsQuery
    fun topArtistsByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int
    ): List<ArtistPreview> {
        return artistService.findTopByPopularity(page = page, pageSize = size)
    }

}