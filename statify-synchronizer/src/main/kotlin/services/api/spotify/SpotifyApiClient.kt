package org.danila.services.api.spotify

import event.TokenCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.AlbumSimpleDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.danila.services.spotify.TokenStore
import org.danila.util.SpotifyFetchContextKey
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

    suspend fun getAllFollowedArtists(): List<ArtistDTO> {
        val context = coroutineContext[SpotifyFetchContextKey] ?: throw IllegalStateException("No fetch context found")

        val artistLimit = context.artistLimit
        var artistAfter = context.artistAfter
        var remainingArtists = context.artistTotal

        val allArtists = mutableListOf<ArtistDTO>()

        while (allArtists.size < artistLimit) {
            val limit = minOf(remainingArtists ?: FETCH_ARTISTS_MAX_LIMIT, FETCH_ARTISTS_MAX_LIMIT)

            val response = withRetryAfter {
                withAuthRetry { authHeader ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getFollowedArtists(authHeader = authHeader, after = artistAfter, limit = limit).artists
                    }
                }
            }

            allArtists.addAll(response.items)
            artistAfter = response.cursors.after
            remainingArtists = maxOf((remainingArtists ?: response.total) - response.items.size, 0)

            if (response.next == null || allArtists.size >= artistLimit) break
        }

        context.updateFetchOptions(artistAfter = artistAfter, artistTotal = remainingArtists ?: 0)

        return allArtists
    }

    suspend fun getSeveralArtists(
        artistIds: Set<String>,
    ): List<ArtistDTO> =
        artistIds.chunked(50).flatMap { chunk ->
            withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralArtists(authHeader = auth, ids = chunk.joinToString(",")).artists
                    }
                }
            }
        }

    suspend fun getAllSavedAlbums(): List<SavedAlbumItemDTO> {
        val context = coroutineContext[SpotifyFetchContextKey] ?: throw IllegalStateException("No fetch context found")

        val albumLimit = context.albumLimit
        var albumOffset = context.albumOffset ?: 0
        var remainingAlbums = context.albumTotal

        val allAlbums = mutableListOf<SavedAlbumItemDTO>()

        while (allAlbums.size < albumLimit) {
            val limit = minOf(remainingAlbums ?: FETCH_ALBUMS_MAX_LIMIT, FETCH_ALBUMS_MAX_LIMIT)

            val response = withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSavedAlbums(
                            authHeader = auth,
                            limit = limit,
                            offset = albumOffset
                        )
                    }
                }
            }

            allAlbums.addAll(response.items.map { it.copy(album = it.album.normalized()) })
            albumOffset += response.limit
            remainingAlbums = maxOf((remainingAlbums ?: response.total) - response.items.size, 0)

            if (response.next == null || allAlbums.size >= albumLimit) break
        }

        context.updateFetchOptions(albumOffset = albumOffset, albumTotal = remainingAlbums ?: 0)

        return allAlbums
    }

    suspend fun getSeveralAlbums(
        albumIds: Set<String>,
    ): List<AlbumDTO> =
        albumIds.chunked(20).flatMap { chunk ->
            withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralAlbums(authHeader = auth, ids = chunk.joinToString(",")).albums.map { it.normalized() }
                    }
                }
            }
        }

    suspend fun getAllSavedTracks(): List<SavedTrackItemDTO> {
        val context = coroutineContext[SpotifyFetchContextKey] ?: throw IllegalStateException("No fetch context found")

        val trackLimit = context.trackLimit
        var trackOffset = context.trackOffset ?: 0
        var remainingTracks = context.trackTotal

        val allTracks = mutableListOf<SavedTrackItemDTO>()

        while (allTracks.size < trackLimit) {
            val limit = minOf(remainingTracks ?: FETCH_TRACKS_MAX_LIMIT, FETCH_TRACKS_MAX_LIMIT)

            val response = withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSavedTracks(
                            authHeader = auth,
                            limit = limit,
                            offset = trackOffset
                        )
                    }
                }
            }

            allTracks.addAll(response.items.map { it.normalized() })
            trackOffset += response.limit
            remainingTracks = maxOf((remainingTracks ?: response.total) - response.items.size, 0)

            if (response.next == null || allTracks.size >= trackLimit) break
        }

        context.updateFetchOptions(trackOffset = trackOffset, trackTotal = remainingTracks ?: 0)

        return allTracks
    }

    suspend fun getSeveralTracks(
        trackIds: Set<String>,
    ): List<TrackDTO> =
        trackIds.chunked(50).flatMap { chunk ->
            withRetryAfter {
                withAuthRetry { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralTracks(authHeader = auth, ids = chunk.joinToString(",")).tracks.map { it.normalized() }
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