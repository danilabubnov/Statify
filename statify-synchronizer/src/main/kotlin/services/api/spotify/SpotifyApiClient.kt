package org.danila.services.api.spotify

import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpotifyApiClient @Autowired constructor(
    private val spotifyApi: SpotifyAPI
) {

    suspend fun getAllFollowedArtists(authHeader: String): List<ArtistDTO> {
        val allArtists = mutableListOf<ArtistDTO>()
        var after: String? = null

        do {
            val response = spotifyApi.getFollowedArtists(
                authHeader = authHeader,
                after = after
            ).artists

            allArtists.addAll(response.items)

            after = response.cursors.after
        } while (response.next != null)

        return allArtists
    }

    suspend fun getSeveralArtists(authHeader: String, artistIds: Set<String>): List<ArtistDTO> {
        return artistIds.chunked(50).flatMap { chunk ->
            spotifyApi.getSeveralArtists(authHeader = authHeader, ids = chunk.joinToString(",")).artists
        }
    }

    suspend fun getAllSavedAlbums(authHeader: String): List<SavedAlbumItemDTO> {
        val allAlbums = mutableListOf<SavedAlbumItemDTO>()
        var offset = 0

        do {
            val response = spotifyApi.getSavedAlbums(
                authHeader = authHeader,
                limit = 50,
                offset = offset
            )

            allAlbums.addAll(response.items)

            offset += response.limit
        } while (response.next != null)

        return allAlbums
    }

    suspend fun getSeveralAlbums(authHeader: String, albumIds: Set<String>): List<AlbumDTO> {
        return albumIds.chunked(20).flatMap { chunk ->
            spotifyApi.getSeveralAlbums(authHeader = authHeader, ids = chunk.joinToString(",")).albums
        }
    }

    suspend fun getAllSavedTracks(authHeader: String): List<SavedTrackItemDTO> {
        val allTracks = mutableListOf<SavedTrackItemDTO>()
        var offset = 0

        do {
            val response = spotifyApi.getSavedTracks(
                authHeader = authHeader,
                limit = 50,
                offset = offset
            )

            allTracks.addAll(response.items)

            offset += response.limit
        } while (response.next != null)

        return allTracks
    }

    suspend fun getSeveralTracks(authHeader: String, trackIds: Set<String>): List<TrackDTO> {
        return trackIds.chunked(50).flatMap { chunk ->
            spotifyApi.getSeveralTracks(authHeader = authHeader, ids = chunk.joinToString(",")).tracks
        }
    }

}