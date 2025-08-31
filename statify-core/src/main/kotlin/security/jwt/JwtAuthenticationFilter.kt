package org.danila.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.danila.security.PublicEndpoints
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val publicEndpoints: PublicEndpoints,
    private val jwtUtils: JwtUtils
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = publicEndpoints.isPublicEndpoint(request)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = parseBearerToken(request)

        if (token != null && jwtUtils.validateToken(token = token, type = TokenType.ACCESS)) {
            val username = jwtUtils.getUsernameFromToken(token = token, type = TokenType.ACCESS)
            val userDetails = userDetailsService.loadUserByUsername(username)

            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        }

        filterChain.doFilter(request, response)
    }

    private fun parseBearerToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization") ?: return null

        return if (!bearerToken.startsWith("Bearer ")) null
        else bearerToken.substring(7)
    }

}