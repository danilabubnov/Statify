package org.danila.web.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.Image
import org.danila.repository.AlbumRepository
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.MappedBatchLoaderWithContext
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@Component
@DgsDataLoader(name = AlbumCoversDataLoader.DGS_DATA_LOADER_NAME)
class AlbumCoversDataLoader(
    private val albumRepository: AlbumRepository
) : MappedBatchLoaderWithContext<String, List<Image>> {

    companion object {
        const val DGS_DATA_LOADER_NAME = "albumCoversDataLoader"
    }

    override fun load(
        keys: Set<String>,
        environment: BatchLoaderEnvironment
    ): CompletionStage<Map<String, List<Image>>> =
        CompletableFuture.supplyAsync {
            if  (keys.isEmpty()) return@supplyAsync emptyMap<String, List<Image>>()

            val rows = albumRepository.findImagesForAlbumIds(keys)

            rows.groupBy { it.getAlbumId() }.mapValues { (_, albumImages) ->
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