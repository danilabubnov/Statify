package org.danila.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher

@Component
class PublicEndpoints {

    val publicEndpoints: Set<String> = setOf(
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        "/configuration/**",
        "/favicon.ico",
        "/api/auth/register",
        "/api/auth/login",
        "/login/oauth2/code/**",
        "/actuator/health"
    )

    val excludedEndpoints: Set<String> = setOf(
        "/api/auth/me",
        "/api/auth/link/spotify"
    )

    fun isPublicEndpoint(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        val matcher = AntPathMatcher()

        val isPublic = publicEndpoints.any { endpoint -> matcher.match(endpoint, path) }
        val isExcluded = excludedEndpoints.any { endpoint -> matcher.match(endpoint, path) }

        return isPublic && !isExcluded
    }
}