package org.danila.services.api.spotify

import org.danila.dto.album.FullAlbumsResponseDTO
import org.danila.dto.album.SavedAlbumsResponseDTO
import org.danila.dto.artist.FollowingArtistsResponseDTO
import org.danila.dto.artist.FullArtistsResponseDTO
import org.danila.dto.track.FullTracksResponseDTO
import org.danila.dto.track.SavedTracksResponseDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyAPI {

    @GET("v1/me/tracks")
    suspend fun getSavedTracks(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SavedTracksResponseDTO

    @GET("v1/me/following")
    suspend fun getFollowedArtists(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = 50,
        @Query("after") after: String? = null
    ): FollowingArtistsResponseDTO

    @GET("v1/me/albums")
    suspend fun getSavedAlbums(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 50,
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