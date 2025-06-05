package org.danila.security.oauth2

import event.TokenCredentials
import event.UserConnectedEvent
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.danila.configuration.USER_SPOTIFY_CONNECTED_TOPIC
import org.danila.model.OAuth2LinkState
import org.danila.repository.OAuth2LinkStateRepository
import org.danila.service.model.spotify.SpotifyInfoService
import org.danila.service.model.spotify.UserSpotifyLibraryService
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class OAuth2SuccessHandler(
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val oAuth2LinkStateRepository: OAuth2LinkStateRepository,
    private val userSpotifyLibraryService: UserSpotifyLibraryService,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val spotifyInfoService: SpotifyInfoService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val httpRequest = requireNotNull(request) { "HttpServletRequest is null" }
        val httpResponse = requireNotNull(response) { "HttpServletResponse is null" }
        val auth = requireNotNull(authentication) { "Authentication is null" }

        val stateId = httpRequest.getParameter("state") ?: error("Missing state parameter")
        val oAuth2LinkState = oAuth2LinkStateRepository.findById(UUID.fromString(stateId))
            .orElseThrow { IllegalArgumentException("Unknown state") }

        if (auth is OAuth2AuthenticationToken && auth.principal is SpotifyOAuth2User) {
            handleSpotifyAuthenticationSuccess(
                spotifyUser = auth.principal as SpotifyOAuth2User,
                state = oAuth2LinkState,
                authorizedClient = authorizedClientService.loadAuthorizedClient(
                    auth.authorizedClientRegistrationId,
                    auth.name
                ) ?: error("Authorized client not found")
            )
        } else error("Unsupported authentication type")

        oAuth2LinkStateRepository.delete(oAuth2LinkState)
        httpResponse.status = HttpStatus.OK.value()
        httpResponse.writer.write("Spotify account linked successfully")
    }

    fun handleSpotifyAuthenticationSuccess(spotifyUser: SpotifyOAuth2User, state: OAuth2LinkState, authorizedClient: OAuth2AuthorizedClient) {
        val user = state.user
        val spotifyId = spotifyUser.name
        val refreshToken = authorizedClient.refreshToken?.tokenValue ?: error("Refresh token not set")
        val spotifyInfo = spotifyInfoService.findBySpotifyIdOrNull(spotifyId) ?: error("Unknown spotifyId")

        spotifyInfoService.update(
            spotifyInfo.copy(
                user = user,
                refreshToken = refreshToken
            )
        )

        val userSpotifyLibraryService = userSpotifyLibraryService.create(user)

        kafkaTemplate.send(
            USER_SPOTIFY_CONNECTED_TOPIC,
            UserConnectedEvent(
                eventId = UUID.randomUUID(),
                userId = user.id,
                timestamp = Instant.now(),
                tokenCredentials = TokenCredentials(
                    accessToken = authorizedClient.accessToken.tokenValue,
                    refreshToken = refreshToken
                ),
                userSpotifyLibraryId = userSpotifyLibraryService.id
            )
        )
    }

}