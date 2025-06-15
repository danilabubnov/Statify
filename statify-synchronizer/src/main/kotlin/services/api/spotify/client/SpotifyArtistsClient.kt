package org.danila.services.api.spotify.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_ARTISTS_PER_MULTI_FETCH
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.artist.FollowingArtistsResponseDTO
import org.danila.dto.artist.FullArtistsResponseDTO
import org.danila.exception.spotifyApi.SpotifyCircuitBreakerOpenException
import org.danila.exception.spotifyApi.SpotifyServerErrorException
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

    fun getAllFollowedArtists(): Flow<ArtistDTO> = flow {
        var after: String? = null

        do {
            val artists = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { authHeader ->
                    getFollowedArtistsPage(authHeader = authHeader, after = after).artists
                }
            }

            artists.items.forEach { emit(it) }

            after = artists.cursors.after
        } while (artists.next != null)
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFollowingArtistsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFollowingArtistsResponseDTO")
    suspend fun getFollowedArtistsPage(authHeader: String, after: String?): FollowingArtistsResponseDTO {
        return withContext(Dispatchers.IO) {
            spotifyApi.getFollowedArtists(
                authHeader = authHeader,
                after = after
            )
        }
    }

    private suspend fun spotifyServerErrorRetryFollowingArtistsResponseDTO(throwable: Throwable): FollowingArtistsResponseDTO {
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFollowingArtistsResponseDTO(throwable: Throwable): FollowingArtistsResponseDTO {
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

    suspend fun getSeveralArtists(
        artistIds: Set<String>,
    ): Flow<ArtistDTO> = flow {
        artistIds.chunked(MAX_ARTISTS_PER_MULTI_FETCH).forEach { chunk ->
            val artists = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSeveralArtistsPage(
                        authHeader = auth,
                        artistIds = chunk
                    ).artists
                }
            }

            artists.forEach { emit(it) }
        }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullArtistsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullArtistsResponseDTO")
    suspend fun getSeveralArtistsPage(authHeader: String, artistIds: List<String>): FullArtistsResponseDTO {
        return withContext(Dispatchers.IO) {
            spotifyApi.getSeveralArtists(
                authHeader = authHeader,
                ids = artistIds.joinToString(",")
            )
        }
    }

    private suspend fun spotifyServerErrorRetryFullArtistsResponseDTO(throwable: Throwable): FullArtistsResponseDTO {
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFullArtistsResponseDTO(throwable: Throwable): FullArtistsResponseDTO {
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

}