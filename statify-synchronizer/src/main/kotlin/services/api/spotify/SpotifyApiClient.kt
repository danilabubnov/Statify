package org.danila.services.api.spotify

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import event.TokenCredentials
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.danila.MAX_ALBUMS_PER_MULTI_FETCH
import org.danila.MAX_ARTISTS_PER_MULTI_FETCH
import org.danila.MAX_TRACKS_PER_MULTI_FETCH
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.FullAlbumsResponseDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.album.SavedAlbumsResponseDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.artist.FollowingArtistsResponseDTO
import org.danila.dto.artist.FullArtistsResponseDTO
import org.danila.dto.track.FullTracksResponseDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.SavedTracksResponseDTO
import org.danila.dto.track.TrackDTO
import org.danila.services.spotify.TokenStore
import org.danila.util.UserIdKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

@Service
class SpotifyApiClient @Autowired constructor(
    private val spotifyAuthService: SpotifyAuthService,
    private val spotifyApi: SpotifyAPI,
    private val tokenStore: TokenStore,
) {

    fun getAllFollowedArtists(): Flow<ArtistDTO> = flow {
        var after: String? = null

        do {
            val artists = withRetryAfter {
                withAuthRetry { authHeader ->
                    getFollowedArtistsPage(authHeader = authHeader, after = after)?.artists
                }
            }

            artists?.items?.forEach { emit(it) }

            after = artists?.cursors?.after
        } while (artists?.next != null)
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFollowingArtistsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFollowingArtistsResponseDTO")
    suspend fun getFollowedArtistsPage(authHeader: String, after: String?): FollowingArtistsResponseDTO? {
        return spotifyApi.getFollowedArtists(
            authHeader = authHeader,
            after = after
        )
    }

    private suspend fun spotifyServerErrorRetryFollowingArtistsResponseDTO(throwable: Throwable): FollowingArtistsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownFollowingArtistsResponseDTO(throwable: Throwable): FollowingArtistsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    suspend fun getSeveralArtists(
        artistIds: Set<String>,
    ): Flow<ArtistDTO> = flow {
        artistIds.chunked(MAX_ARTISTS_PER_MULTI_FETCH).forEach { chunk ->
            val artists = withRetryAfter {
                withAuthRetry { auth ->
                    getSeveralArtistsPage(
                        authHeader = auth,
                        artistIds = chunk
                    )?.artists
                }
            }

            artists?.forEach { emit(it) }
        }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullArtistsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullArtistsResponseDTO")
    suspend fun getSeveralArtistsPage(authHeader: String, artistIds: List<String>): FullArtistsResponseDTO? {
        return spotifyApi.getSeveralArtists(
            authHeader = authHeader,
            ids = artistIds.joinToString(",")
        )
    }

    private suspend fun spotifyServerErrorRetryFullArtistsResponseDTO(throwable: Throwable): FullArtistsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownFullArtistsResponseDTO(throwable: Throwable): FullArtistsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    fun getAllSavedAlbums(): Flow<SavedAlbumItemDTO> = flow {
        var offset = 0

        do {
            val albums = withRetryAfter {
                withAuthRetry { auth ->
                    getAllSavedAlbumsPage(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            albums?.items?.forEach { emit(it.copy(album = it.album.normalized())) }

            if (albums?.limit != null) offset += albums.limit
        } while (albums?.next != null)
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetrySavedAlbumsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownSavedAlbumsResponseDTO")
    suspend fun getAllSavedAlbumsPage(authHeader: String, offset: Int): SavedAlbumsResponseDTO? {
        return spotifyApi.getSavedAlbums(
            authHeader = authHeader,
            offset = offset
        )
    }

    private suspend fun spotifyServerErrorRetrySavedAlbumsResponseDTO(throwable: Throwable): SavedAlbumsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownSavedAlbumsResponseDTO(throwable: Throwable): SavedAlbumsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    suspend fun getSeveralAlbums(
        albumIds: Set<String>,
    ): Flow<AlbumDTO> = flow {
        albumIds.chunked(MAX_ALBUMS_PER_MULTI_FETCH).forEach { chunk ->
            val albums = withRetryAfter {
                withAuthRetry { auth ->
                    getSeveralAlbumsPage(
                        authHeader = auth,
                        albumsIds = chunk
                    )?.albums
                }
            }

            albums?.forEach { emit(it.normalized()) }
        }
    }

    /**
     * This method is declared public and open **only** to allow Spring AOP proxying for Resilience4j annotations.
     * It is intended for internal use within this class and should **not** be called directly from outside.
     */
    @Retry(name = "spotifyServerErrorRetry", fallbackMethod = "spotifyServerErrorRetryFullAlbumsResponseDTO")
    @CircuitBreaker(name = "spotifyCircuitBreaker", fallbackMethod = "onSpotifyServiceDownFullAlbumsResponseDTO")
    suspend fun getSeveralAlbumsPage(authHeader: String, albumsIds: List<String>): FullAlbumsResponseDTO? {
        return spotifyApi.getSeveralAlbums(
            authHeader = authHeader,
            ids = albumsIds.joinToString(",")
        )
    }

    private suspend fun spotifyServerErrorRetryFullAlbumsResponseDTO(throwable: Throwable): FullAlbumsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownFullAlbumsResponseDTO(throwable: Throwable): FullAlbumsResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    fun getAllSavedTracks(): Flow<SavedTrackItemDTO> = flow {
        var offset = 0

        do {
            val tracks = withRetryAfter {
                withAuthRetry { auth ->
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
        return spotifyApi.getSavedTracks(
            authHeader = authHeader,
            offset = offset
        )
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
            val tracks = withRetryAfter {
                withAuthRetry { auth ->
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
        return spotifyApi.getSeveralTracks(authHeader = authHeader, ids = tracksIds.joinToString(","))
    }

    private suspend fun spotifyServerErrorRetryFullTracksResponseDTO(throwable: Throwable): FullTracksResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private suspend fun onSpotifyServiceDownFullTracksResponseDTO(throwable: Throwable): FullTracksResponseDTO? {
        // TODO: implement logging and sending to kafka and set userLibrary.sync = ERROR_IN_PROCESS
        return null
    }

    private val userTokenMutexCache: Cache<UUID, Mutex> = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build()

    private suspend inline fun <T> withAuthRetry(
        crossinline block: suspend (authHeader: String) -> T
    ): T {
        val userId = coroutineContext[UserIdKey]?.userId ?: throw IllegalStateException("No userId found")
        var creds = tokenStore.get(userId)
        val initial = creds.accessToken

        return try {
            block("Bearer $initial")
        } catch (e: HttpException) {
            if (e.code() != 401) throw e

            val userTokenMutex = userTokenMutexCache.get(userId) { Mutex() }

            val newToken = userTokenMutex.withLock {
                creds = tokenStore.get(userId)

                if (creds.accessToken == initial) {
                    val fresh = spotifyAuthService.refreshAccessToken(creds.refreshToken)

                    tokenStore.put(userId, TokenCredentials(fresh, creds.refreshToken))

                    fresh
                } else creds.accessToken
            }

            block("Bearer $newToken")
        }
    }

    private suspend inline fun <T> withRetryAfter(
        maxRetries: Int = 3,
        crossinline block: suspend () -> T
    ): T {
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: HttpException) {
                if (e.code() != 429) throw e

                val retryAfterSec = e.response()?.headers()?.get("Retry-After")?.toLongOrNull() ?: run {
                    println("Retry-After header is missing, using default value of 5 seconds")
                    5L
                }

                println("Rate limited, attempt ${attempt + 1}/$maxRetries — retrying after $retryAfterSec s")

                delay(retryAfterSec * 1_000L)
            }
        }

        return block()
    }

}