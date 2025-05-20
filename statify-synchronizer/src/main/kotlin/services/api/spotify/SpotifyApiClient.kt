package org.danila.services.api.spotify

import event.TokenCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.danila.MAX_ALBUMS_PER_MULTI_FETCH
import org.danila.MAX_ARTISTS_PER_MULTI_FETCH
import org.danila.MAX_TRACKS_PER_MULTI_FETCH
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.AlbumSimpleDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.danila.services.spotify.TokenStore
import org.danila.util.UserIdKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import retrofit2.HttpException
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
            val response = withRetryAfter {
                withAuthRetry { authHeader ->
                    spotifyApi.getFollowedArtists(
                        authHeader = authHeader,
                        after = after
                    ).artists
                }
            }

            response.items.forEach { emit(it) }

            after = response.cursors.after
        } while (response.next != null)
    }

    suspend fun getSeveralArtists(
        artistIds: Set<String>,
    ): Flow<ArtistDTO> = flow {
        artistIds.chunked(MAX_ARTISTS_PER_MULTI_FETCH).forEach { chunk ->
            withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralArtists(authHeader = auth, ids = chunk.joinToString(",")).artists.forEach { emit(it) }
                    }
                }
            }
        }
    }

    fun getAllSavedAlbums(): Flow<SavedAlbumItemDTO> = flow {
        var offset = 0

        do {
            val response = withRetryAfter {
                withAuthRetry { auth ->
                    spotifyApi.getSavedAlbums(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            response.items.forEach { emit(it.copy(album = it.album.normalized())) }
            offset += response.limit
        } while (response.next != null)
    }

    suspend fun getSeveralAlbums(
        albumIds: Set<String>,
    ): Flow<AlbumDTO> = flow {
        albumIds.chunked(MAX_ALBUMS_PER_MULTI_FETCH).forEach { chunk ->
            withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralAlbums(authHeader = auth, ids = chunk.joinToString(",")).albums.forEach { emit(it.normalized()) }
                    }
                }
            }
        }
    }

    fun getAllSavedTracks(): Flow<SavedTrackItemDTO> = flow {
        var offset = 0

        do {
            val response = withRetryAfter {
                withAuthRetry { auth ->
                    spotifyApi.getSavedTracks(
                        authHeader = auth,
                        offset = offset
                    )
                }
            }

            response.items.forEach { emit(it.normalized()) }
            offset += response.limit
        } while (response.next != null)
    }

    suspend fun getSeveralTracks(
        trackIds: Set<String>,
    ): Flow<TrackDTO> = flow {
        trackIds.chunked(MAX_TRACKS_PER_MULTI_FETCH).forEach { chunk ->
            withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralTracks(authHeader = auth, ids = chunk.joinToString(",")).tracks.forEach { emit(it.normalized()) }
                    }
                }
            }
        }
    }

    private fun AlbumDTO.normalized(): AlbumDTO =
        this.copy(
            albumType = this.albumType.uppercase(),
            releaseDatePrecision = this.releaseDatePrecision.uppercase()
        )

    private fun AlbumSimpleDTO.normalized(): AlbumSimpleDTO =
        this.copy(
            albumType = this.albumType.uppercase(),
            releaseDatePrecision = this.releaseDatePrecision.uppercase()
        )

    private fun TrackDTO.normalized(): TrackDTO =
        this.copy(
            album = this.album.normalized()
        )

    private fun SavedTrackItemDTO.normalized(): SavedTrackItemDTO =
        this.copy(
            track = this.track.normalized()
        )

    private val tokenMutex = Mutex()

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

            val newToken = tokenMutex.withLock {
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