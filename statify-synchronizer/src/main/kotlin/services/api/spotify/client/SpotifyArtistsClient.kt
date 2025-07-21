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
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_ARTISTS_PER_MULTI_FETCH
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.artist.FollowingArtistsResponseDTO
import org.danila.dto.artist.FullArtistsResponseDTO
import org.danila.services.api.spotify.auth.SpotifyAuthRetryHelper
import org.danila.services.api.spotify.retry.SpotifyRateLimitRetryHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpotifyArtistsClient @Autowired constructor(
    private val spotifyAuthRetryHelper: SpotifyAuthRetryHelper,
    private val spotifyRateLimitRetryHelper: SpotifyRateLimitRetryHelper,
    private val spotifyApi: SpotifyAPI,
) {

    private val logger by logger()

    fun getAllFollowedArtists(): Flow<ArtistDTO> = flow {
        logger.debug { "Starting paginated retrieval of all followed artists" }

        var after: String? = null

        do {
            logger.debug { "Requesting followed artists page with after=$after" }

            val artists = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { authHeader ->
                    getFollowedArtistsPage(authHeader = authHeader, after = after).artists
                }
            }

            logger.debug {
                "Received FollowingArtists page: itemsCount=${artists.items.size}, " +
                        "hasNext=${artists.next != null}"
            }

            artists.items.forEach { emit(it) }

            after = artists.cursors.after
        } while (artists.next != null)

        logger.debug { "Completed retrieval of all followed artists" }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFollowingArtistsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFollowingArtistsResponseDTO")
    suspend fun getFollowedArtistsPage(authHeader: String, after: String?): FollowingArtistsResponseDTO {
        logger.debug { "Executing HTTP request getFollowedArtists(after=$after)" }

        return withContext(Dispatchers.IO) {
            spotifyApi.getFollowedArtists(
                authHeader = authHeader,
                after = after
            )
        }
    }

    private suspend fun spotifyServerErrorRetryFollowingArtistsResponseDTO(throwable: Throwable): FollowingArtistsResponseDTO {
        logger.debug { "spotifyServerErrorRetryFollowingArtistsResponseDTO fallback executed" }
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFollowingArtistsResponseDTO(throwable: Throwable): FollowingArtistsResponseDTO {
        logger.debug { "onSpotifyServiceDownFollowingArtistsResponseDTO fallback executed" }
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

    suspend fun getSeveralArtists(
        artistIds: Set<String>,
    ): Flow<ArtistDTO> = flow {
        logger.debug { "Starting multi-fetch of artist details for ${artistIds.size} artists" }

        artistIds.chunked(MAX_ARTISTS_PER_MULTI_FETCH).forEach { chunk ->
            logger.debug { "Requesting getSeveralArtistsPage for chunkSize=${chunk.size}" }

            val artists = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSeveralArtistsPage(
                        authHeader = auth,
                        artistIds = chunk
                    ).artists
                }
            }

            logger.debug { "Received ${artists.size} artists in multi-fetch response" }

            artists.forEach { emit(it) }
        }

        logger.debug { "Completed multi-fetch of artist details" }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullArtistsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullArtistsResponseDTO")
    suspend fun getSeveralArtistsPage(authHeader: String, artistIds: List<String>): FullArtistsResponseDTO {
        logger.debug { "Executing HTTP request getSeveralArtists(ids=${artistIds.joinToString(",")})" }

        return withContext(Dispatchers.IO) {
            spotifyApi.getSeveralArtists(
                authHeader = authHeader,
                ids = artistIds.joinToString(",")
            )
        }
    }

    private suspend fun spotifyServerErrorRetryFullArtistsResponseDTO(throwable: Throwable): FullArtistsResponseDTO {
        logger.debug { "spotifyServerErrorRetryFullArtistsResponseDTO fallback executed" }
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFullArtistsResponseDTO(throwable: Throwable): FullArtistsResponseDTO {
        logger.debug { "onSpotifyServiceDownFullArtistsResponseDTO fallback executed" }
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

}