package org.danila.services.api.spotify.client

import exception.spotifyapi.SpotifyCircuitBreakerOpenException
import exception.spotifyapi.SpotifyServerErrorException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import logging.logger
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

    private val logger by logger()

    fun getAllSavedTracks(): Flow<SavedTrackItemDTO> = flow {
        logger.debug { "Starting paginated retrieval of all saved tracks" }

        var offset = 0

        do {
            logger.debug { "Requesting saved tracks page with offset=$offset" }

            val tracks = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSavedTracksPage(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            logger.debug {
                "Received SavedTracks page: itemsCount=${tracks.items.size}, " +
                        "limit=${tracks.limit}, hasNext=${tracks.next != null}"
            }

            tracks.items.forEach { emit(it.normalized()) }

            offset += tracks.limit
        } while (tracks.next != null)

        logger.debug { "Completed retrieval of all saved tracks" }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetrySavedTracksResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownSavedTracksResponseDTO")
    suspend fun getSavedTracksPage(authHeader: String, offset: Int): SavedTracksResponseDTO {
        logger.debug { "Executing HTTP request getSavedTracks(offset=$offset)" }

        return withContext(Dispatchers.IO) {
            spotifyApi.getSavedTracks(
                authHeader = authHeader,
                offset = offset
            )
        }
    }

    private suspend fun spotifyServerErrorRetrySavedTracksResponseDTO(throwable: Throwable): SavedTracksResponseDTO {
        logger.debug { "spotifyServerErrorRetrySavedTracksResponseDTO fallback executed" }
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownSavedTracksResponseDTO(throwable: Throwable): SavedTracksResponseDTO {
        logger.debug { "onSpotifyServiceDownSavedTracksResponseDTO fallback executed" }
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

    suspend fun getSeveralTracks(
        trackIds: Set<String>,
    ): Flow<TrackDTO> = flow {
        logger.debug { "Starting multi-fetch of track details for ${trackIds.size} tracks" }

        trackIds.chunked(MAX_TRACKS_PER_MULTI_FETCH).forEach { chunk ->
            logger.debug { "Requesting getSeveralTracksPage for chunkSize=${chunk.size}" }

            val tracks = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSeveralTracksPage(
                        authHeader = auth,
                        tracksIds = chunk
                    ).tracks
                }
            }

            logger.debug { "Received ${tracks.size} tracks in multi-fetch response" }

            tracks.forEach { emit(it.normalized()) }
        }

        logger.debug { "Completed multi-fetch of track details" }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullTracksResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullTracksResponseDTO")
    suspend fun getSeveralTracksPage(authHeader: String, tracksIds: List<String>): FullTracksResponseDTO {
        logger.debug { "Executing HTTP request getSeveralTracks(ids=${tracksIds.joinToString(",")})" }

        return withContext(Dispatchers.IO) {
            spotifyApi.getSeveralTracks(authHeader = authHeader, ids = tracksIds.joinToString(","))
        }
    }

    private suspend fun spotifyServerErrorRetryFullTracksResponseDTO(throwable: Throwable): FullTracksResponseDTO {
        logger.debug { "spotifyServerErrorRetryFullTracksResponseDTO fallback executed" }
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFullTracksResponseDTO(throwable: Throwable): FullTracksResponseDTO {
        logger.debug { "onSpotifyServiceDownFullTracksResponseDTO fallback executed" }
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

}