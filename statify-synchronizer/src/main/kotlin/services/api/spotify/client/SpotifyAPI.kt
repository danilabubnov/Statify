package org.danila.services.api.spotify.client

import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_FOLLOWED_ARTISTS_PAGE_SIZE
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_SAVED_ALBUMS_PAGE_SIZE
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_SAVED_TRACKS_PAGE_SIZE
import org.danila.dto.spotify.album.FullAlbumsResponseDTO
import org.danila.dto.spotify.album.SavedAlbumsResponseDTO
import org.danila.dto.spotify.artist.FollowingArtistsResponseDTO
import org.danila.dto.spotify.artist.FullArtistsResponseDTO
import org.danila.dto.spotify.track.FullTracksResponseDTO
import org.danila.dto.spotify.track.SavedTracksResponseDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyAPI {

    @GET("v1/me/tracks")
    suspend fun getSavedTracks(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = MAX_SAVED_TRACKS_PAGE_SIZE,
        @Query("offset") offset: Int = 0
    ): SavedTracksResponseDTO

    @GET("v1/me/following")
    suspend fun getFollowedArtists(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = MAX_FOLLOWED_ARTISTS_PAGE_SIZE,
        @Query("after") after: String? = null
    ): FollowingArtistsResponseDTO

    @GET("v1/me/albums")
    suspend fun getSavedAlbums(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = MAX_SAVED_ALBUMS_PAGE_SIZE,
        @Query("offset") offset: Int = 0
    ): SavedAlbumsResponseDTO

    @GET("v1/albums")
    suspend fun getSeveralAlbums(
        @Header("Authorization") authHeader: String,
        @Query("ids") ids: String
    ): FullAlbumsResponseDTO

    @GET("v1/tracks")
    suspend fun getSeveralTracks(
        @Header("Authorization") authHeader: String,
        @Query("ids") ids: String
    ): FullTracksResponseDTO

    @GET("v1/artists")
    suspend fun getSeveralArtists(
        @Header("Authorization") authHeader: String,
        @Query("ids") ids: String
    ): FullArtistsResponseDTO

}