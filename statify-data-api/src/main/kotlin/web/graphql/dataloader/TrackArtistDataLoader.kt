package org.danila.web.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import org.danila.generated.types.ArtistSimple
import org.danila.repository.ArtistRepository
import org.dataloader.BatchLoaderEnvironment
import org.dataloader.MappedBatchLoaderWithContext
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
@DgsDataLoader(name = TrackArtistsDataLoader.DGS_DATA_LOADER_NAME)
class TrackArtistsDataLoader(
    private val artistRepository: ArtistRepository
) : MappedBatchLoaderWithContext<String, List<ArtistSimple>> {

    companion object {
        const val DGS_DATA_LOADER_NAME = "trackArtistsDataLoader"
    }

    override fun load(
        keys: Set<String>,
        environment: BatchLoaderEnvironment
    ): CompletableFuture<Map<String, List<ArtistSimple>>> = CompletableFuture.supplyAsync {
        if  (keys.isEmpty()) return@supplyAsync emptyMap<String, List<ArtistSimple>>()

        val rows = artistRepository.findArtistsByTrackIds(keys)

        rows.groupBy { it.getTrackId() }
            .mapValues { (_, list) ->
                list.map { r ->
                    ArtistSimple(
                        id = r.getArtistId(),
                        name = r.getName(),
                        popularity = r.getPopularity(),
                        followersTotal = r.getFollowersTotal()
                    )
                }
            }
    }

}