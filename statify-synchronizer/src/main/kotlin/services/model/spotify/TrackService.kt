package org.danila.services.model.spotify

import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_TRACKS_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.track.Track
import org.danila.repository.TrackRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class TrackService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val trackRepository: TrackRepository
) {

    suspend fun findExistingTracks(ids: Set<String>): List<Track> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            trackRepository.findTracksBySpotifyIdIn(ids).awaitList()
        }

    suspend fun upsertAndReturnSimpleTracks(tracks: Collection<Track>): Collection<String> =
        tracks
            .sortedBy { it.spotifyId }
            .chunked(MAX_SAVED_TRACKS_CHUNK_SIZE)
            .flatMap { chunk ->
                DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                    trackRepository.upsertAndReturnSimpleTracks(chunk).awaitList()
                }
            }

}