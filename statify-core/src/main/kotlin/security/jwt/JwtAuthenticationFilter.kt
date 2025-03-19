package org.danila.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.danila.exception.InvalidBearerTokenException
import org.danila.exception.MissingBearerTokenException
import org.danila.security.PublicEndpoints
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.filter.OncePerRequestFilter

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
        val token = parseJwt(request)

        if (jwtUtils.validateToken(token)) {
            val username = jwtUtils.getUsernameFromToken(token)
            val userDetails = userDetailsService.loadUserByUsername(username)

            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        } else throw MissingBearerTokenException()

        filterChain.doFilter(request, response)
    }

    private fun parseJwt(request: HttpServletRequest): String {
        val bearerToken = request.getHeader("Authorization")

        if (bearerToken == null) throw MissingBearerTokenException()
        else if (!bearerToken.startsWith("Bearer ")) throw InvalidBearerTokenException()
        else return bearerToken.substring(7)
    }

}