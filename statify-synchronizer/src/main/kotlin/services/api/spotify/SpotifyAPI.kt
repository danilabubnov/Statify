package org.danila.services.api.spotify

import org.danila.dto.album.FullAlbumsResponseDTO
import org.danila.dto.album.SavedAlbumsResponseDTO
import org.danila.dto.artist.FollowingArtistsResponseDTO
import org.danila.dto.artist.FullArtistsResponseDTO
import org.danila.dto.track.FullTracksResponseDTO
import org.danila.dto.track.SavedTracksResponseDTO
import org.danila.dto.track.SavedTracksTotalDTO
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

const val FETCH_TRACKS_MAX_LIMIT = 50
const val FETCH_ARTISTS_MAX_LIMIT = 50
const val FETCH_ALBUMS_MAX_LIMIT = 50

interface SpotifyAPI {

    @GET("v1/me/tracks")
    suspend fun getSavedTracksTotal(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 1,
        @Query("offset") offset: Int = 0
    ): SavedTracksTotalDTO

    @GET("v1/me/tracks")
    suspend fun getSavedTracks(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = FETCH_TRACKS_MAX_LIMIT,
        @Query("offset") offset: Int = 0
    ): SavedTracksResponseDTO

    @GET("v1/me/following")
    suspend fun getFollowedArtists(
        @Header("Authorization") authHeader: String,
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = FETCH_ARTISTS_MAX_LIMIT,
        @Query("after") after: String? = null
    ): FollowingArtistsResponseDTO

    @GET("v1/me/albums")
    suspend fun getSavedAlbums(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = FETCH_ALBUMS_MAX_LIMIT,
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