package org.danila.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtils: JwtUtils
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = parseBearerToken(request)

        if (token != null && jwtUtils.validateToken(token)) {
            val userId = jwtUtils.getUserIdFromToken(token)
            val principal = JwtUserPrincipal(userId = userId)
            val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun parseBearerToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization") ?: return null

        return if (!bearerToken.startsWith("Bearer ")) null
        else bearerToken.substring(7)
    }

}
