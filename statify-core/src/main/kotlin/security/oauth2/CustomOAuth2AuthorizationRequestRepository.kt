package org.danila.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.danila.repository.OAuth2LinkStateRepository
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomOAuth2AuthorizationRequestRepository(
    private val oAuth2LinkStateRepository: OAuth2LinkStateRepository
): AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    override fun loadAuthorizationRequest(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
        val state = UUID.fromString(request?.getParameter("state"))

        return oAuth2LinkStateRepository.findById(state)
            .map { it.authorizationRequest }
            .orElse(null)
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest?,
        response: HttpServletResponse?
    ) {}

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        val state = UUID.fromString(request.getParameter("state"))

        return oAuth2LinkStateRepository.findById(state)
            .map { it.authorizationRequest }
            .orElse(null)
    }

}