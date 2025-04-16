package org.danila.services.api.spotify

import org.danila.dto.auth.SpotifyAuthResponseDTO
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface SpotifyAuthAPI {

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): SpotifyAuthResponseDTO

}