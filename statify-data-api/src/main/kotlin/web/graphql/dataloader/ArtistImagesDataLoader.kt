package org.danila.web.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.Image
import org.danila.repository.ArtistRepository
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.MappedBatchLoaderWithContext
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@Component
@DgsDataLoader(name = ArtistImagesDataLoader.DGS_DATA_LOADER_NAME)
class ArtistImagesDataLoader(
    private val artistRepository: ArtistRepository
) : MappedBatchLoaderWithContext<String, List<Image>> {

    companion object {
        const val DGS_DATA_LOADER_NAME = "artistImagesDataLoader"
    }

    override fun load(
        keys: Set<String>,
        environment: BatchLoaderEnvironment
    ): CompletionStage<Map<String, List<Image>>> = CompletableFuture.supplyAsync {
        if  (keys.isEmpty()) return@supplyAsync emptyMap<String, List<Image>>()

        val rows = artistRepository.findImagesForArtistIds(keys)

        rows.groupBy { it.getArtistId() }
            .mapValues { (_, artistImages) ->
                artistImages.sortedBy { it.getImageOrder() }.map { artistImage ->
                    Image(
                        imageUrl = artistImage.getImageUrl(),
                        imageHeight = artistImage.getImageHeight(),
                        imageWidth = artistImage.getImageWidth()
                    )
                }
            }
    }

}