package org.danila.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.danila.configuration.USER_SPOTIFY_CONNECTED_TOPIC
import org.danila.events.UserConnectedEvent
import org.danila.events.UserConnectedMetadata
import org.danila.repository.OAuth2LinkStateRepository
import org.danila.service.SpotifyInfoService
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class OAuth2SuccessHandler(
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val oAuth2LinkStateRepository: OAuth2LinkStateRepository,
    private val kafkaTemplate: KafkaTemplate<String, UserConnectedEvent>,
    private val spotifyInfoService: SpotifyInfoService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val httpRequest = request ?: throw IllegalStateException("HttpServletRequest is null")
        val httpResponse = response ?: throw IllegalStateException("HttpServletResponse is null")
        val auth = authentication ?: throw IllegalStateException("Authentication is null")

        val stateParam = httpRequest.getParameter("state") ?: throw IllegalStateException("Missing state parameter")
        val oAuth2LinkState = oAuth2LinkStateRepository.findById(UUID.fromString(stateParam)).orElse(null) ?: throw IllegalArgumentException("Unknown state")

        if (auth is OAuth2AuthenticationToken) {
            val authorizedClient: OAuth2AuthorizedClient = authorizedClientService.loadAuthorizedClient(auth.authorizedClientRegistrationId, auth.name)
                ?: throw IllegalStateException("Authorized client not found")

            val accessToken = authorizedClient.accessToken.tokenValue
            val expiresAt = authorizedClient.accessToken.expiresAt ?: throw IllegalArgumentException("Access token expiresAt not set")
            val refreshToken = authorizedClient.refreshToken?.tokenValue ?: throw IllegalArgumentException("Refresh token not set")

            val spotifyUser = auth.principal as? SpotifyOAuth2User ?: throw IllegalStateException("Principal is not a SpotifyOAuth2User")
            val spotifyId = spotifyUser.name

            val user = oAuth2LinkState.user

            val spotifyInfo = spotifyInfoService.findBySpotifyIdOrNull(spotifyId) ?: throw IllegalArgumentException("Unknown spotifyId")

            spotifyInfoService.update(
                spotifyInfo.copy(
                    user = user,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = expiresAt
                )
            )

            kafkaTemplate.send(
                USER_SPOTIFY_CONNECTED_TOPIC, UserConnectedEvent(
                    eventId = UUID.randomUUID(),
                    userId = user.id,
                    eventType = "USER_SPOTIFY_CONNECTED",
                    timestamp = Instant.now(),
                    metadata = UserConnectedMetadata(
                        spotifyId = spotifyId,
                        accessToken = accessToken,
                        refreshToken = refreshToken
                    )
                )
            )
        } else {
            throw IllegalStateException("Authentication is not of type OAuth2AuthenticationToken")
        }

        oAuth2LinkStateRepository.delete(oAuth2LinkState)

        httpResponse.writer.write("Spotify account linked successfully")
        httpResponse.status = HttpStatus.OK.value()
    }

}