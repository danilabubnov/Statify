package org.danila.web.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.FavTrack
import org.danila.generated.types.Image
import org.danila.web.graphql.dataloader.TrackArtistsDataLoader
import org.danila.web.graphql.dataloader.TrackCoversDataLoader
import java.util.concurrent.CompletableFuture

@DgsComponent
class FavTrackFieldFetcher {

    @DgsData(parentType = "FavTrack", field = "artists")
    fun fetchArtistsForFavTrack(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<ArtistSimple>> {
        val track = dfe.getSource<FavTrack>()!!
        val loader = dfe.getDataLoader<String, List<ArtistSimple>>(TrackArtistsDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(track.id)
    }

    @DgsData(parentType = "FavTrack", field = "covers")
    fun fetchCoversForFavTrack(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Image>> {
        val track = dfe.getSource<FavTrack>()!!
        val loader = dfe.getDataLoader<String, List<Image>>(TrackCoversDataLoader.DGS_DATA_LOADER_NAME)!!

        return loader.load(track.id)
    }

}
