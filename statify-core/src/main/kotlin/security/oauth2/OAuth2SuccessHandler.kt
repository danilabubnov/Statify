package org.danila.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.danila.model.OAuth2LinkState
import org.danila.repository.OAuth2LinkStateRepository
import org.danila.service.SpotifyInfoService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class OAuth2SuccessHandler(
    private val oAuth2LinkStateRepository: OAuth2LinkStateRepository,
    private val spotifyInfoService: SpotifyInfoService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val stateId = request?.getParameter("state")?.let { UUID.fromString(it) } ?: throw IllegalStateException("Missing state parameter")
        val oAuth2LinkState = oAuth2LinkStateRepository.findById(stateId).getOrNull() ?: throw IllegalArgumentException("Unknown state")

        handleAccountLinking(oAuth2LinkState, authentication!!, response!!)

        oAuth2LinkStateRepository.delete(oAuth2LinkState)
    }

    private fun handleAccountLinking(
        state: OAuth2LinkState,
        authentication: Authentication,
        response: HttpServletResponse
    ) {
        val spotifyId = (authentication.principal as SpotifyOAuth2User).name

        val spotifyInfo = spotifyInfoService.findBySpotifyIdOrNull(spotifyId) ?: throw IllegalArgumentException("Unknown spotifyId")

        spotifyInfoService.setUser(spotifyInfo = spotifyInfo, user = state.user)

        response.writer.write("Spotify account linked successfully")
        response.status = HttpStatus.OK.value()
    }

}