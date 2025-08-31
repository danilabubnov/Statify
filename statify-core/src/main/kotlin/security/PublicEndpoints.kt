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
        "/api/auth/refresh",
        "/api/auth/logout",
        "/login/oauth2/code/**",
        "/actuator/health"
    )

    fun isPublicEndpoint(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        val matcher = AntPathMatcher()

        val isPublic = publicEndpoints.any { endpoint -> matcher.match(endpoint, path) }

        return isPublic
    }

}