package org.danila.web.controller

import org.danila.model.OAuth2LinkState
import org.danila.repository.OAuth2LinkStateRepository
import org.danila.security.user.UserDetailsImpl
import org.danila.service.utils.IdGeneratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/oauth")
class OAuthController @Autowired constructor(
    private val oAuth2LinkStateRepository: OAuth2LinkStateRepository,
    private val idGeneratorService: IdGeneratorService,

    @Value("\${spring.security.oauth2.client.registration.spotify.client-id}") private val spotifyClientId: String,
    @Value("\${spring.security.oauth2.client.registration.spotify.redirect-uri}") private val spotifyRedirectUri: String,
    @Value("\${spring.security.oauth2.client.registration.spotify.scope}") private val spotifyScope: String,
    @Value("\${spring.security.oauth2.client.provider.spotify.authorization-uri}") private val spotifyAuthorizeUri: String
) {

    @PostMapping("/link/spotify")
    fun initiateLinkSpotify(@AuthenticationPrincipal userDetails: UserDetailsImpl): ResponseEntity<Void> {
        val state = idGeneratorService.uuid
        val authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("$spotifyAuthorizeUri?show_dialog=true")
            .clientId(spotifyClientId)
            .redirectUri(spotifyRedirectUri)
            .scopes(spotifyScope.split(",").toSet())
            .state(state.toString())
            .attributes(mapOf("registration_id" to "spotify"))
            .build()

        oAuth2LinkStateRepository.save(
            OAuth2LinkState(
                id = state,
                authorizationRequest = authorizationRequest,
                user = userDetails.user
            )
        )

        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", authorizationRequest.authorizationRequestUri)
            .build()
    }

}