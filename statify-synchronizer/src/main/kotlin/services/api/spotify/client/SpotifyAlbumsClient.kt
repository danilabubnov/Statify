package org.danila.services.api.spotify.client

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_ALBUMS_PER_MULTI_FETCH
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.FullAlbumsResponseDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.album.SavedAlbumsResponseDTO
import exception.spotifyapi.SpotifyCircuitBreakerOpenException
import exception.spotifyapi.SpotifyServerErrorException
import org.danila.services.api.spotify.auth.SpotifyAuthRetryHelper
import org.danila.services.api.spotify.retry.SpotifyRateLimitRetryHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpotifyAlbumsClient @Autowired constructor(
    private val spotifyAuthRetryHelper: SpotifyAuthRetryHelper,
    private val spotifyRateLimitRetryHelper: SpotifyRateLimitRetryHelper,
    private val spotifyApi: SpotifyAPI,
) {

    fun getAllSavedAlbums(): Flow<SavedAlbumItemDTO> = flow {
        var offset = 0

        do {
            val albums = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getAllSavedAlbumsPage(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            albums.items.forEach { emit(it.copy(album = it.album.normalized())) }

            offset += albums.limit
        } while (albums.next != null)
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetrySavedAlbumsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownSavedAlbumsResponseDTO")
    suspend fun getAllSavedAlbumsPage(authHeader: String, offset: Int): SavedAlbumsResponseDTO {
        return withContext(Dispatchers.IO) {
            spotifyApi.getSavedAlbums(
                authHeader = authHeader,
                offset = offset
            )
        }
    }

    private suspend fun spotifyServerErrorRetrySavedAlbumsResponseDTO(throwable: Throwable): SavedAlbumsResponseDTO {
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownSavedAlbumsResponseDTO(throwable: Throwable): SavedAlbumsResponseDTO {
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

    suspend fun getSeveralAlbums(
        albumIds: Set<String>,
    ): Flow<AlbumDTO> = flow {
        albumIds.chunked(MAX_ALBUMS_PER_MULTI_FETCH).forEach { chunk ->
            val albums = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSeveralAlbumsPage(
                        authHeader = auth,
                        albumsIds = chunk
                    ).albums
                }
            }

            albums.forEach { emit(it.normalized()) }
        }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullAlbumsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullAlbumsResponseDTO")
    suspend fun getSeveralAlbumsPage(authHeader: String, albumsIds: List<String>): FullAlbumsResponseDTO {
        return withContext(Dispatchers.IO) {
            spotifyApi.getSeveralAlbums(
                authHeader = authHeader,
                ids = albumsIds.joinToString(",")
            )
        }
    }

    private suspend fun spotifyServerErrorRetryFullAlbumsResponseDTO(throwable: Throwable): FullAlbumsResponseDTO {
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFullAlbumsResponseDTO(throwable: Throwable): FullAlbumsResponseDTO {
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

}