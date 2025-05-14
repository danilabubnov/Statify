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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import retrofit2.HttpException

@Service
class SpotifyApiClient @Autowired constructor(
    private val spotifyAuthService: SpotifyAuthService,
    private val spotifyApi: SpotifyAPI,
) {

    suspend fun getAllFollowedArtists(tokenCredentials: TokenCredentials): List<ArtistDTO> {
        val allArtists = mutableListOf<ArtistDTO>()
        var after: String? = null

        do {
            val response = withRetryAfter {
                withAuthRetry(tokenCredentials) { authHeader ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getFollowedArtists(
                            authHeader = authHeader,
                            after = after
                        ).artists
                    }
                }
            }

            allArtists.addAll(response.items)

            after = response.cursors.after
        } while (response.next != null)

        return allArtists
    }

    suspend fun getSeveralArtists(
        tokenCredentials: TokenCredentials,
        artistIds: Set<String>
    ): List<ArtistDTO> =
        artistIds.chunked(50).flatMap { chunk ->
            withRetryAfter {
                withAuthRetry(tokenCredentials) { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralArtists(authHeader = auth, ids = chunk.joinToString(",")).artists
                    }
                }
            }
        }

    suspend fun getAllSavedAlbums(tokenCredentials: TokenCredentials): List<SavedAlbumItemDTO> {
        val allAlbums = mutableListOf<SavedAlbumItemDTO>()
        var offset = 0

        do {
            val response = withRetryAfter {
                withAuthRetry(tokenCredentials) { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSavedAlbums(
                            authHeader = auth,
                            limit = 50,
                            offset = offset
                        )
                    }
                }
            }

            allAlbums += response.items.map { it.copy(album = it.album.normalized()) }
            offset += response.limit
        } while (response.next != null)

        return allAlbums
    }

    suspend fun getSeveralAlbums(
        tokenCredentials: TokenCredentials,
        albumIds: Set<String>
    ): List<AlbumDTO> =
        albumIds.chunked(20).flatMap { chunk ->
            withRetryAfter {
                withAuthRetry(tokenCredentials) { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSeveralAlbums(authHeader = auth, ids = chunk.joinToString(",")).albums.map { it.normalized() }
                    }
                }
            }
        }

    suspend fun getAllSavedTracks(tokenCredentials: TokenCredentials): List<SavedTrackItemDTO> {
        val allTracks = mutableListOf<SavedTrackItemDTO>()
        var offset = 0

        do {
            val response = withRetryAfter {
                withAuthRetry(tokenCredentials) { auth ->
                    withContext(Dispatchers.IO) {
                        spotifyApi.getSavedTracks(
                            authHeader = auth,
                            limit = 50,
                            offset = offset
                        )
                    }
                }
            }

            allTracks += response.items.map { it.normalized() }
            offset += response.limit
        } while (response.next != null)

        return allTracks
    }

    suspend fun getSeveralTracks(
        tokenCredentials: TokenCredentials,
        trackIds: Set<String>
    ): List<TrackDTO> =
        trackIds.chunked(50).flatMap { chunk ->
            withRetryAfter {
                withAuthRetry(tokenCredentials) { auth ->
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
        tokenCredentials: TokenCredentials,
        crossinline block: suspend (authHeader: String) -> T
    ): T {
        val initialToken = tokenCredentials.accessToken

        return try {
            block("Bearer $initialToken")
        } catch (e: HttpException) {
            if (e.code() != 401) throw e

            val newAuth = tokenMutex.withLock {
                if (tokenCredentials.accessToken == initialToken) {
                    val refreshed = spotifyAuthService.refreshAccessToken(tokenCredentials.refreshToken)

                    tokenCredentials.accessToken = refreshed

                    "Bearer $refreshed"
                } else "Bearer ${tokenCredentials.accessToken}"
            }

            block(newAuth)
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