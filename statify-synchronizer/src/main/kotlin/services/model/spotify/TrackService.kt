package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.danila.awaitList
import org.danila.model.spotify.track.Track
import org.danila.repository.TrackRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class TrackService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val trackRepository: TrackRepository
) {

    suspend fun findExistingTracks(ids: Set<String>): List<Track> =
        readSemaphore.withPermit { trackRepository.findTracksBySpotifyIdIn(ids).awaitList() }

    suspend fun upsertTracks(tracks: Collection<Track>): Collection<Track> =
        writeSemaphore.withPermit {
            transactionalOperator.executeAndAwait {
                tracks.chunked(300)
                    .map { chunk ->
                        trackRepository.upsertBatch(chunk)
                            .collectList()
                            .awaitSingle()
                    }
                    .flatten()
            }
        }

}