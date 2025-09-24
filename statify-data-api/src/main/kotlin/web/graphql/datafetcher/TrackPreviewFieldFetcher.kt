package org.danila.web.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.Image
import org.danila.generated.types.TrackPreview
import org.danila.web.graphql.dataloader.TrackArtistsDataLoader
import org.danila.web.graphql.dataloader.TrackCoversDataLoader
import java.util.concurrent.CompletableFuture

@DgsComponent
class TrackPreviewFieldFetcher {

    @DgsData(parentType = "TrackPreview", field = "artists")
    fun fetchArtistsForTrackPreview(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<ArtistSimple>> {
        val track = dfe.getSource<TrackPreview>()!!
        val loader = dfe.getDataLoader<String, List<ArtistSimple>>(TrackArtistsDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(track.id)
    }

    @DgsData(parentType = "TrackPreview", field = "covers")
    fun fetchCoversForTrackPreview(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Image>> {
        val track = dfe.getSource<TrackPreview>()!!
        val loader = dfe.getDataLoader<String, List<Image>>(TrackCoversDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(track.id)
    }

}