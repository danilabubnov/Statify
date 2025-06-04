package org.danila.services.model.spotify.storage

import org.danila.configuration.variables.SpotifyChunkSizes.MAX_SAVED_TRACKS_CHUNK_SIZE
import org.danila.model.spotify.track.Track
import org.danila.repository.TrackRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TrackStorageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val trackRepository: TrackRepository
) {

    suspend fun findExistingTracks(ids: Set<String>): List<Track> =
        databaseExecutionContext.withRead {
            trackRepository.findTracksBySpotifyIdIn(ids).awaitList()
        }

    suspend fun upsertAndReturnSimpleTracks(tracks: Collection<Track>): Collection<String> =
        tracks
            .sortedBy { it.spotifyId }
            .chunked(MAX_SAVED_TRACKS_CHUNK_SIZE)
            .flatMap { chunk ->
                databaseExecutionContext.withWriteTransactionRetry {
                    trackRepository.upsertAndReturnSimpleTracks(chunk).awaitList()
                }
            }

}