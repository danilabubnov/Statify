package org.danila.web.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.Image
import org.danila.repository.TrackRepository
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.MappedBatchLoaderWithContext
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@Component
@DgsDataLoader(name = TrackCoversDataLoader.DGS_DATA_LOADER_NAME)
class TrackCoversDataLoader(
    private val trackRepository: TrackRepository
) : MappedBatchLoaderWithContext<String, List<Image>> {

    companion object {
        const val DGS_DATA_LOADER_NAME = "trackCoversDataLoader"
    }

    override fun load(
        keys: Set<String>,
        environment: BatchLoaderEnvironment
    ): CompletionStage<Map<String, List<Image>>> = CompletableFuture.supplyAsync {
        if  (keys.isEmpty()) return@supplyAsync emptyMap<String, List<Image>>()

        val rows = trackRepository.findImagesForTrackIds(keys)

        rows.groupBy { it.getTrackId() }.mapValues { (_, albumImages) ->
            albumImages.sortedBy { it.getImageOrder() }.map { albumImage ->
                Image(
                    imageUrl = albumImage.getImageUrl(),
                    imageHeight = albumImage.getImageHeight(),
                    imageWidth = albumImage.getImageWidth()
                )
            }
        }
    }

}