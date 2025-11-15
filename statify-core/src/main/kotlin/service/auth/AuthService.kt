package org.danila.service.auth

import org.danila.dto.request.auth.LoginRequest
import org.danila.dto.request.auth.RegistrationRequest
import org.danila.dto.response.auth.AuthResponse
import org.danila.security.jwt.JwtUtils
import org.danila.security.jwt.TokenType
import org.danila.security.user.UserDetailsImpl
import org.danila.service.model.user.UserService
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

    fun register(request: RegistrationRequest): RegisterResult {
        val createdUser = userService.create(
            email = request.email,
            password = request.password
        )

        val accessToken = jwtUtils.generateToken(userId = createdUser.id, type = TokenType.ACCESS)
        val refreshToken = jwtUtils.generateToken(userId = createdUser.id, type = TokenType.REFRESH)

        return RegisterResult(accessToken = accessToken, refreshToken = refreshToken, user = AuthResponse(id = createdUser.id, email = createdUser.email))
    }

    fun login(request: LoginRequest): LoginResult {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        SecurityContextHolder.getContext().authentication = authentication

        val userDetails = authentication.principal as UserDetailsImpl
        val userId = userDetails.user.id

        val accessToken = jwtUtils.generateToken(userId = userId, type = TokenType.ACCESS)
        val refreshToken = jwtUtils.generateToken(userId = userId, type = TokenType.REFRESH)

        return LoginResult(accessToken = accessToken, refreshToken = refreshToken, user = AuthResponse(id = userDetails.user.id, email = userDetails.user.email))
    }

    fun refreshAccessToken(refreshToken: String): String {
        if (!jwtUtils.validateToken(token = refreshToken, type = TokenType.REFRESH)) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token")

        val userId = jwtUtils.getUserIdFromToken(token = refreshToken, type = TokenType.REFRESH)
        val newAccessToken = jwtUtils.generateToken(userId = userId, type = TokenType.ACCESS)

        return newAccessToken
    }

}

data class RegisterResult(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthResponse
)

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthResponse
)