package org.danila.security.oauth2

import org.danila.model.spotify.SpotifyInfo
import org.danila.service.SpotifyInfoService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class SpotifyOAuth2UserService(private val spotifyInfoService: SpotifyInfoService) : DefaultOAuth2UserService() {

    override fun loadUser(oauth2UserRequest: OAuth2UserRequest?): OAuth2User? {
        val oAuth2User = super.loadUser(oauth2UserRequest)

        return processOAuth2User(oAuth2User = oAuth2User)
    }

    private fun processOAuth2User(oAuth2User: OAuth2User): SpotifyOAuth2User {
        val attributes = oAuth2User.attributes
        val spotifyId = attributes["id"] as? String ?: throw IllegalArgumentException("SpotifyId not found")
        val email = attributes["email"] as? String ?: throw IllegalStateException("Email not found")

        val spotifyInfo = spotifyInfoService.findBySpotifyIdOrNull(spotifyId)

        if (spotifyInfo != null && spotifyInfo.user != null) throw IllegalStateException("Spotify account already linked")

        return SpotifyOAuth2User(
            spotifyInfo = spotifyInfo ?: spotifyInfoService.create(spotifyId = spotifyId, email = email, user = null),
            oauth2Attributes = attributes
        )
    }

}

class SpotifyOAuth2User(
    private val spotifyInfo: SpotifyInfo,
    private val oauth2Attributes: Map<String, Any>
) : OAuth2User {

    override fun getName(): String = spotifyInfo.spotifyId
    override fun getAttributes(): Map<String, Any> = oauth2Attributes
    override fun getAuthorities(): Collection<GrantedAuthority> = emptySet()

}