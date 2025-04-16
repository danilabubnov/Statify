package org.danila.services.api.spotify

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class SpotifyAuthService @Autowired constructor(
    private val spotifyAuthApi: SpotifyAuthAPI,

    @Value("\${spring.security.oauth2.client.registration.spotify.client-secret}") private val spotifyClientSecret: String,
    @Value("\${spring.security.oauth2.client.registration.spotify.client-id}") private val spotifyClientId: String

) {

    fun isAccessTokenExpired(expiresAt: Instant): Boolean =
        Instant.now().isAfter(expiresAt.minusSeconds(60))

    suspend fun refreshAccessToken(refreshToken: String): String {
        val authString = "$spotifyClientId:$spotifyClientSecret"
        val encodedAuth = Base64.getEncoder().encodeToString(authString.toByteArray())

        val response = spotifyAuthApi.refreshToken(
            authHeader = "Basic $encodedAuth",
            grantType = "refresh_token",
            refreshToken = refreshToken
        )

        return response.accessToken
    }

}