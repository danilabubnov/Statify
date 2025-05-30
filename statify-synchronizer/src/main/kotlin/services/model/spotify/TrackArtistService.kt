package org.danila.services.model.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.TrackArtist
import org.danila.repository.TrackArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class TrackArtistService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val trackArtistsRepository: TrackArtistRepository
) {

    suspend fun findExistingTrackArtists(ids: Set<Pair<String, String>>): List<TrackArtist> =
        withContext(Dispatchers.IO) {
            readSemaphore.withPermit { trackArtistsRepository.findByTrackArtistPairs(ids).awaitList() }
        }

    suspend fun persistTrackArtists(trackArtists: Collection<TrackArtist>): Unit =
        withContext(Dispatchers.IO) {
            writeSemaphore.withPermit {
                trackArtists.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
                    transactionalOperator.executeAndAwait {
                        trackArtistsRepository.insertBatch(chunk)
                            .awaitSingleOrNull()
                    }
                }
            }
        }

}