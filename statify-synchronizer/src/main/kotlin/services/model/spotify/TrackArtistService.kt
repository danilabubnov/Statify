package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.TrackArtist
import org.danila.repository.TrackArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class TrackArtistService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val trackArtistsRepository: TrackArtistRepository
) {

    suspend fun findExistingTrackArtists(ids: Set<Pair<String, String>>): List<TrackArtist> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            trackArtistsRepository.findByTrackArtistPairs(ids).awaitList()
        }

    suspend fun persistTrackArtists(trackArtists: Collection<TrackArtist>) {
        trackArtists.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                trackArtistsRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}