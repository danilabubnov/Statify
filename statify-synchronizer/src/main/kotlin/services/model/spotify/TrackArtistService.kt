package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
        readSemaphore.withPermit { trackArtistsRepository.findByTrackArtistPairs(ids).awaitList() }

    suspend fun persistTrackArtists(trackArtists: Collection<TrackArtist>): Collection<TrackArtist> =
        writeSemaphore.withPermit {
            transactionalOperator.executeAndAwait {
                trackArtists.chunked(300)
                    .map { chunk ->
                        trackArtistsRepository.insertBatch(chunk)
                            .collectList()
                            .awaitSingle()
                    }
                    .flatten()
            }
        }

}