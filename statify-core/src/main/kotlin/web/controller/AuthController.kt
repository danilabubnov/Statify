package org.danila.web.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.danila.dto.request.auth.LoginRequest
import org.danila.dto.request.auth.RegistrationRequest
import org.danila.dto.response.auth.AuthResponse
import org.danila.dto.response.auth.LoginResponse
import org.danila.dto.response.auth.RegisterResponse
import org.danila.dto.response.auth.TokenResponse
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
import java.net.URI

@RestController
@RequestMapping("/api/auth")
class AuthController @Autowired constructor(
    private val authService: AuthService,

    @Value("\${jwt.refresh.expiration.ms}") private val refreshExpirationMs: Long,
) {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    fun whoIam(@AuthenticationPrincipal userDetailsImpl: UserDetailsImpl): AuthResponse =
        AuthResponse.fromUser(userDetailsImpl.user)

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegistrationRequest): ResponseEntity<RegisterResponse> {
        val registerResult = authService.register(request.normalize())

        return ResponseEntity.created(URI.create("/profile/${registerResult.user.id}"))
            .header(
                HttpHeaders.SET_COOKIE, createCookie(value = registerResult.refreshToken, maxAgeSec = refreshExpirationMs / 1000)
                    .toString()
            ).body(RegisterResponse(accessToken = registerResult.accessToken, user = registerResult.user))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val loginResult = authService.login(request.normalize())

        return ResponseEntity.ok()
            .header(
                HttpHeaders.SET_COOKIE, createCookie(value = loginResult.refreshToken, maxAgeSec = refreshExpirationMs / 1000)
                    .toString()
            ).body(LoginResponse(accessToken = loginResult.accessToken, user = loginResult.user))
    }

    @PostMapping("/refresh")
    fun refresh(@CookieValue("refreshToken") refreshToken: String?): ResponseEntity<TokenResponse> {
        if (refreshToken.isNullOrBlank()) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        else return ResponseEntity.ok(TokenResponse(accessToken = authService.refreshAccessToken(refreshToken)))
    }

    // todo: store and then invalidate access token
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    fun logout(): ResponseEntity<Void> = ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, createCookie(value = "", maxAgeSec = 0).toString()).build()

    private fun createCookie(value: String, maxAgeSec: Long) =
        ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(false)        // prod: true
            .path("/api/auth/refresh")
            .sameSite("Lax")      // prod: Lax/Strict
            .maxAge(maxAgeSec)
            .build()

}