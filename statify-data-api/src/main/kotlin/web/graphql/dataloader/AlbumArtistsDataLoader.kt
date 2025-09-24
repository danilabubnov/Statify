package org.danila.web.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import org.danila.generated.types.ArtistSimple
import org.danila.repository.ArtistRepository
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.MappedBatchLoaderWithContext
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
@DgsDataLoader(name = AlbumArtistsDataLoader.DGS_DATA_LOADER_NAME)
class AlbumArtistsDataLoader(
    private val artistRepository: ArtistRepository
) : MappedBatchLoaderWithContext<String, List<ArtistSimple>> {

    companion object {
        const val DGS_DATA_LOADER_NAME = "albumArtistsDataLoader"
    }

    override fun load(
        keys: Set<String>,
        environment: BatchLoaderEnvironment
    ): CompletableFuture<Map<String, List<ArtistSimple>>> = CompletableFuture.supplyAsync {
        if  (keys.isEmpty()) return@supplyAsync emptyMap<String, List<ArtistSimple>>()

        val rows = artistRepository.findArtistsByAlbumIds(keys)

        rows.groupBy { it.getAlbumId() }.mapValues { (_, artistByAlbums) ->
            artistByAlbums.map { artistByAlbum ->
                ArtistSimple(
                    id = artistByAlbum.getArtistId(),
                    name = artistByAlbum.getName(),
                    popularity = artistByAlbum.getPopularity(),
                    followersTotal = artistByAlbum.getFollowersTotal()
                )
            }
        }
    }

}