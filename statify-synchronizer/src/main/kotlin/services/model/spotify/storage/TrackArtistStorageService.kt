package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.TrackArtist
import org.danila.repository.TrackArtistRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TrackArtistStorageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val trackArtistsRepository: TrackArtistRepository
) {

    suspend fun findExistingTrackArtists(ids: Set<Pair<String, String>>): List<TrackArtist> =
        databaseExecutionContext.withRead {
            trackArtistsRepository.findByTrackArtistPairs(ids).awaitList()
        }

    suspend fun persistTrackArtists(trackArtists: Collection<TrackArtist>) {
        trackArtists.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                trackArtistsRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}