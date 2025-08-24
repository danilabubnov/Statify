package org.danila.web.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.danila.dto.auth.AuthResponse
import org.danila.dto.auth.LoginRequest
import org.danila.dto.auth.RegistrationRequest
import org.danila.dto.auth.TokenResponse
import org.danila.model.users.User
import org.danila.security.user.UserDetailsImpl
import org.danila.service.auth.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/auth")
class AuthController @Autowired constructor(
    private val authService: AuthService,

    @Value("\${jwt.refresh.expiration.ms}") private val refreshExpirationMs: Long,
) {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    fun whoIam(@AuthenticationPrincipal userDetailsImpl: UserDetailsImpl): AuthResponse =
        userDetailsImpl.user.toUserResponse()

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegistrationRequest): AuthResponse =
        authService.register(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val loginResponse = authService.login(request)

        return ResponseEntity.ok()
            .header(
                HttpHeaders.SET_COOKIE, refreshCookie(value = loginResponse.refreshToken, maxAgeSec = refreshExpirationMs / 1000)
                    .toString()
            ).body(TokenResponse(loginResponse.accessToken))
    }

    @PostMapping("/refresh")
    fun refresh(@CookieValue("refreshToken") refreshToken: String?): ResponseEntity<TokenResponse> {
        if (refreshToken.isNullOrBlank()) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        else return ResponseEntity.ok(authService.refresh(refreshToken))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Void> = ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, refreshCookie(value = "", maxAgeSec = 0).toString()).build()

    private fun refreshCookie(value: String, maxAgeSec: Long) =
        ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(false)        // prod: true
            .path("/api/auth/refresh")
            .sameSite("Lax")      // prod: Lax/Strict
            .maxAge(maxAgeSec)
            .build()

}

fun User.toUserResponse() = AuthResponse(id = id, firstName = firstName, lastName = lastName, email = email, username = username)