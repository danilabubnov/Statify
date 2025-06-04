package org.danila.services.api.spotify.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_TRACKS_PER_MULTI_FETCH
import org.danila.dto.track.FullTracksResponseDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.SavedTracksResponseDTO
import org.danila.dto.track.TrackDTO
import org.danila.services.api.spotify.auth.SpotifyAuthRetryHelper
import org.danila.services.api.spotify.retry.SpotifyRateLimitRetryHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpotifyTracksClient @Autowired constructor(
    private val spotifyAuthRetryHelper: SpotifyAuthRetryHelper,
    private val spotifyRateLimitRetryHelper: SpotifyRateLimitRetryHelper,
    private val spotifyApi: SpotifyAPI,
) {

    fun getAllSavedTracks(): Flow<SavedTrackItemDTO> = flow {
        var offset = 0

        do {
            val tracks = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSavedTracksPage(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            tracks?.items?.forEach { emit(it.normalized()) }
            if (tracks?.limit != null) offset += tracks.limit
        } while (tracks?.next != null)
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetrySavedTracksResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownSavedTracksResponseDTO")
    suspend fun getSavedTracksPage(authHeader: String, offset: Int): SavedTracksResponseDTO? {
        return withContext(Dispatchers.IO) {
            spotifyApi.getSavedTracks(
                authHeader = authHeader,
                offset = offset
            )
        }
    }

    private suspend fun spotifyServerErrorRetrySavedTracksResponseDTO(throwable: Throwable): SavedTracksResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownSavedTracksResponseDTO(throwable: Throwable): SavedTracksResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    suspend fun getSeveralTracks(
        trackIds: Set<String>,
    ): Flow<TrackDTO> = flow {
        trackIds.chunked(MAX_TRACKS_PER_MULTI_FETCH).forEach { chunk ->
            val tracks = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSeveralTracksPage(
                        authHeader = auth,
                        tracksIds = chunk
                    )?.tracks
                }
            }

            tracks?.forEach { emit(it.normalized()) }
        }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullTracksResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullTracksResponseDTO")
    suspend fun getSeveralTracksPage(authHeader: String, tracksIds: List<String>): FullTracksResponseDTO? {
        return withContext(Dispatchers.IO) {
            spotifyApi.getSeveralTracks(authHeader = authHeader, ids = tracksIds.joinToString(","))
        }
    }

    private suspend fun spotifyServerErrorRetryFullTracksResponseDTO(throwable: Throwable): FullTracksResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownFullTracksResponseDTO(throwable: Throwable): FullTracksResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

}