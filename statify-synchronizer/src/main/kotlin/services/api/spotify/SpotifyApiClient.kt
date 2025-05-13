package org.danila.services.api.spotify

import event.TokenCredentials
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

        withAuthRetry(tokenCredentials) { authHeader ->
            val authHeader = "Bearer ${tokenCredentials.accessToken}"

            do {
                val response = spotifyApi.getFollowedArtists(
                    authHeader = authHeader,
                    after = after
                ).artists

                allArtists.addAll(response.items)

                after = response.cursors.after
            } while (response.next != null)
        }

        return allArtists
    }

    suspend fun getSeveralArtists(tokenCredentials: TokenCredentials, artistIds: Set<String>): List<ArtistDTO> {
        return withAuthRetry(tokenCredentials) { authHeader ->
            artistIds.chunked(50).flatMap { chunk ->
                spotifyApi.getSeveralArtists(authHeader = authHeader, ids = chunk.joinToString(",")).artists
            }
        }
    }

    suspend fun getAllSavedAlbums(tokenCredentials: TokenCredentials): List<SavedAlbumItemDTO> {
        val allAlbums = mutableListOf<SavedAlbumItemDTO>()
        var offset = 0

        withAuthRetry(tokenCredentials) { authHeader ->
            do {
                val resp = spotifyApi.getSavedAlbums(
                    authHeader = authHeader,
                    limit = 50,
                    offset = offset
                )

                allAlbums += resp.items.map { it.copy(album = it.album.normalized()) }
                offset += resp.limit
            } while (resp.next != null)
        }

        return allAlbums
    }

    suspend fun getSeveralAlbums(tokenCredentials: TokenCredentials, albumIds: Set<String>): List<AlbumDTO> {
        return withAuthRetry(tokenCredentials) { authHeader ->
            albumIds.chunked(20).flatMap { chunk ->
                spotifyApi.getSeveralAlbums(authHeader = authHeader, ids = chunk.joinToString(",")).albums.map { it.normalized() }
            }
        }
    }

    suspend fun getAllSavedTracks(tokenCredentials: TokenCredentials): List<SavedTrackItemDTO> {
        val allTracks = mutableListOf<SavedTrackItemDTO>()
        var offset = 0

        withAuthRetry(tokenCredentials) { authHeader ->
            do {
                val resp = spotifyApi.getSavedTracks(
                    authHeader = authHeader,
                    limit = 50, offset = offset
                )

                allTracks += resp.items.map { it.normalized() }
                offset += resp.limit
            } while (resp.next != null)
        }

        return allTracks
    }

    suspend fun getSeveralTracks(tokenCredentials: TokenCredentials, trackIds: Set<String>): List<TrackDTO> {
        return withAuthRetry(tokenCredentials) { authHeader ->
            trackIds.chunked(50).flatMap { chunk ->
                spotifyApi.getSeveralTracks(authHeader = authHeader, ids = chunk.joinToString(",")).tracks.map { it.normalized() }
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

    private suspend inline fun <T> withAuthRetry(
        tokenCredentials: TokenCredentials,
        crossinline block: suspend (authHeader: String) -> T
    ): T {
        val initialAuth = "Bearer ${tokenCredentials.accessToken}"

        try {
            return block(initialAuth)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                val newToken = spotifyAuthService.refreshAccessToken(tokenCredentials.refreshToken)
                val retryAuth = "Bearer $newToken"

                tokenCredentials.accessToken = newToken

                return block(retryAuth)
            }

            throw e
        }
    }


}