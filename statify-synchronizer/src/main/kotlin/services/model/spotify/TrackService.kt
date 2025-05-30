package org.danila.services.model.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.danila.MAX_SAVED_TRACKS_CHUNK_SIZE
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
        withContext(Dispatchers.IO) {
            readSemaphore.withPermit { trackRepository.findTracksBySpotifyIdIn(ids).awaitList() }
        }

    suspend fun upsertAndReturnSimpleTracks(tracks: Collection<Track>): Collection<String> =
        withContext(Dispatchers.IO) {
            writeSemaphore.withPermit {
                tracks.chunked(MAX_SAVED_TRACKS_CHUNK_SIZE).flatMap { chunk ->
                    transactionalOperator.executeAndAwait {
                        trackRepository.upsertAndReturnSimpleTracks(chunk)
                            .collectList()
                            .awaitSingle()
                    }
                }
            }
        }

}