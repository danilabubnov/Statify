package org.danila.web.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import org.danila.generated.types.ArtistPreview
import org.danila.generated.types.Image
import org.danila.web.graphql.dataloader.ArtistImagesDataLoader
import java.util.concurrent.CompletableFuture

@DgsComponent
class ArtistPreviewFieldFetcher {

    @DgsData(parentType = "ArtistPreview", field = "images")
    fun fetchImagesForArtistPreview(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Image>> {
        val artist = dfe.getSource<ArtistPreview>()!!
        val loader = dfe.getDataLoader<String, List<Image>>(ArtistImagesDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(artist.id)
    }

}