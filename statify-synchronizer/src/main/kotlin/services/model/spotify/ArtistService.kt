package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.danila.MAX_SAVED_ARTISTS_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.artist.Artist
import org.danila.repository.ArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ArtistService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val artistRepository: ArtistRepository
) {

    suspend fun findExistingArtists(ids: Set<String>): List<Artist> =
        readSemaphore.withPermit { artistRepository.findArtistsBySpotifyIdIn(ids).awaitList() }

    suspend fun upsertAndReturnSimpleArtists(artists: Collection<Artist>): Collection<String> =
        writeSemaphore.withPermit {
            artists.chunked(MAX_SAVED_ARTISTS_CHUNK_SIZE).flatMap { chunk ->
                transactionalOperator.executeAndAwait {
                    artistRepository.upsertAndReturnSimpleArtists(chunk)
                        .collectList()
                        .awaitSingle()
                }
            }
        }

}