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
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_ALBUMS_PER_MULTI_FETCH
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.FullAlbumsResponseDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.album.SavedAlbumsResponseDTO
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

    private val logger by logger()

    fun getAllSavedAlbums(): Flow<SavedAlbumItemDTO> = flow {
        logger.debug { "Starting paginated retrieval of all saved albums" }
        var offset = 0

        do {
            logger.debug { "Requesting saved albums page with offset=$offset" }

            val albums = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getAllSavedAlbumsPage(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            logger.debug {
                "Received SavedAlbums page: itemsCount=${albums.items.size}, " +
                        "limit=${albums.limit}, hasNext=${albums.next != null}"
            }

            albums.items.forEach { emit(it.copy(album = it.album.normalized())) }

            offset += albums.limit
        } while (albums.next != null)

        logger.debug { "Completed retrieval of all saved albums" }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetrySavedAlbumsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownSavedAlbumsResponseDTO")
    suspend fun getAllSavedAlbumsPage(authHeader: String, offset: Int): SavedAlbumsResponseDTO {
        logger.debug { "Executing HTTP request getSavedAlbums(offset=$offset)" }

        return withContext(Dispatchers.IO) {
            spotifyApi.getSavedAlbums(
                authHeader = authHeader,
                offset = offset
            )
        }
    }

    private suspend fun spotifyServerErrorRetrySavedAlbumsResponseDTO(throwable: Throwable): SavedAlbumsResponseDTO {
        logger.debug { "spotifyServerErrorRetrySavedAlbumsResponseDTO fallback executed" }
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownSavedAlbumsResponseDTO(throwable: Throwable): SavedAlbumsResponseDTO {
        logger.debug { "onSpotifyServiceDownSavedAlbumsResponseDTO fallback executed" }
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

    suspend fun getSeveralAlbums(
        albumIds: Set<String>,
    ): Flow<AlbumDTO> = flow {
        logger.debug { "Starting multi-fetch of album details for ${albumIds.size} albums" }

        albumIds.chunked(MAX_ALBUMS_PER_MULTI_FETCH).forEach { chunk ->
            logger.debug { "Requesting getSeveralAlbumsPage for chunkSize=${chunk.size}" }

            val albums = spotifyRateLimitRetryHelper.withRetryAfter {
                spotifyAuthRetryHelper.withAuthRetry { auth ->
                    getSeveralAlbumsPage(
                        authHeader = auth,
                        albumsIds = chunk
                    ).albums
                }
            }

            logger.debug { "Received ${albums.size} albums in multi-fetch response" }

            albums.forEach { emit(it.normalized()) }
        }

        logger.debug { "Completed multi-fetch of album details" }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullAlbumsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullAlbumsResponseDTO")
    suspend fun getSeveralAlbumsPage(authHeader: String, albumsIds: List<String>): FullAlbumsResponseDTO {
        logger.debug { "Executing HTTP request getSeveralAlbums(ids=${albumsIds.joinToString(",")})" }

        return withContext(Dispatchers.IO) {
            spotifyApi.getSeveralAlbums(
                authHeader = authHeader,
                ids = albumsIds.joinToString(",")
            )
        }
    }

    private suspend fun spotifyServerErrorRetryFullAlbumsResponseDTO(throwable: Throwable): FullAlbumsResponseDTO {
        logger.debug { "spotifyServerErrorRetryFullAlbumsResponseDTO fallback executed" }
        throw SpotifyServerErrorException(message = "", cause = throwable)
    }

    private suspend fun onSpotifyServiceDownFullAlbumsResponseDTO(throwable: Throwable): FullAlbumsResponseDTO {
        logger.debug { "onSpotifyServiceDownFullAlbumsResponseDTO fallback executed" }
        throw SpotifyCircuitBreakerOpenException(message = "", cause = throwable)
    }

}