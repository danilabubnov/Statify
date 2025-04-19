package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.awaitList
import org.danila.model.spotify.artist.Artist
import org.danila.repository.ArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ArtistService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val artistRepository: ArtistRepository
) {

    suspend fun findExistingArtists(ids: Set<String>): List<Artist> = artistRepository.findArtistsBySpotifyIdIn(ids).awaitList()

    suspend fun upsertArtists(artists: Collection<Artist>): Collection<Artist> =
        transactionalOperator.executeAndAwait {
            artists.chunked(300)
                .map { chunk ->
                    artistRepository.upsertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}