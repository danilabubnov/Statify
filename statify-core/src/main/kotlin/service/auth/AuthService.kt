package org.danila.service.auth

import org.danila.dto.auth.*
import org.danila.security.jwt.JwtUtils
import org.danila.security.user.UserDetailsImpl
import org.danila.service.model.user.UserService
import org.danila.web.controller.toUserResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtUtils: JwtUtils,
) {

    fun register(request: RegistrationRequest): AuthResponse {
        val sanitizedFirstName = request.firstName.trim().ifEmpty { error("First name must not be blank") }
        val sanitizedLastName = request.lastName.trim().ifEmpty { error("Last name must not be blank") }
        val sanitizedEmail = request.email.trim().ifEmpty { error("Email must not be blank") }
        val sanitizedUsername = request.username.trim().ifEmpty { error("Username must not be blank") }
        val sanitizedPassword = request.password.trim().ifEmpty { error("Password must not be blank") }

        val createdUser = userService.create(
            firstName = sanitizedFirstName,
            lastName = sanitizedLastName,
            email = sanitizedEmail,
            username = sanitizedUsername,
            password = sanitizedPassword
        )

        return createdUser.toUserResponse()
    }

    fun login(request: LoginRequest): LoginResponse {
        val sanitizedUsername = request.username.trim().ifEmpty { error("Username must not be blank") }
        val sanitizedPassword = request.password.trim().ifEmpty { error("Password must not be blank") }

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(sanitizedUsername, sanitizedPassword)
        )
        SecurityContextHolder.getContext().authentication = authentication

        val userDetails = authentication.principal as UserDetailsImpl
        val username = userDetails.username ?: error("Username must not be blank")

        val accessToken = jwtUtils.generateAccessToken(username)
        val refreshToken = jwtUtils.generateRefreshToken(username)

        return LoginResponse(accessToken, refreshToken)
    }

    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtUtils.validateRefreshToken(refreshToken)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token")

        val username = jwtUtils.getUsernameFromRefreshToken(refreshToken)
        val newAccessToken = jwtUtils.generateAccessToken(username)

        return TokenResponse(newAccessToken)
    }

}
