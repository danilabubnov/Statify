package org.danila.web.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import org.danila.generated.types.AlbumPreview
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.Image
import org.danila.web.graphql.dataloader.AlbumArtistsDataLoader
import org.danila.web.graphql.dataloader.AlbumCoversDataLoader
import java.util.concurrent.CompletableFuture

@DgsComponent
class AlbumPreviewFieldFetcher {

    @DgsData(parentType = "AlbumPreview", field = "artists")
    fun fetchArtistsForAlbumPreview(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<ArtistSimple>> {
        val album = dfe.getSource<AlbumPreview>()!!
        val loader = dfe.getDataLoader<String, List<ArtistSimple>>(AlbumArtistsDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(album.id)
    }

    @DgsData(parentType = "AlbumPreview", field = "covers")
    fun fetchCoversForAlbumPreview(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Image>> {
        val album = dfe.getSource<AlbumPreview>()!!
        val loader = dfe.getDataLoader<String, List<Image>>(AlbumCoversDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(album.id)
    }

}