package org.danila.web.controller

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.danila.model.OAuth2LinkState
import org.danila.model.users.User
import org.danila.repository.OAuth2LinkStateRepository
import org.danila.security.jwt.JwtUtils
import org.danila.security.user.UserDetailsImpl
import org.danila.service.UserService
import org.danila.service.utils.IdGeneratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.web.bind.annotation.*
import utils.trimToNull
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController @Autowired constructor(
    private val oAuth2LinkStateRepository: OAuth2LinkStateRepository,
    private val authenticationManager: AuthenticationManager,
    private val idGeneratorService: IdGeneratorService,
    private val userService: UserService,
    private val jwtUtils: JwtUtils,

    @Value("\${spring.security.oauth2.client.registration.spotify.client-id}") private val spotifyClientId: String,
    @Value("\${spring.security.oauth2.client.registration.spotify.redirect-uri}") private val spotifyRedirectUri: String,
    @Value("\${spring.security.oauth2.client.registration.spotify.scope}") private val spotifyScope: String,
    @Value("\${spring.security.oauth2.client.provider.spotify.authorization-uri}") private val spotifyAuthorizeUri: String

) {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    fun whoIam(@AuthenticationPrincipal userDetailsImpl: UserDetailsImpl): UserResponse = userDetailsImpl.user.toUserResponse()

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegistrationRequest): UserResponse {
        val sanitizedFirstName = request.firstName.trimToNull() ?: throw IllegalArgumentException("First name must not be blank")
        val sanitizedLastName = request.lastName.trimToNull() ?: throw IllegalArgumentException("Last name must not be blank")
        val sanitizedEmail = request.email.trimToNull() ?: throw IllegalArgumentException("Email must not be blank")
        val sanitizedUsername = request.username.trimToNull() ?: throw IllegalArgumentException("Username must not be blank")
        val sanitizedPassword = request.password.trimToNull() ?: throw IllegalArgumentException("Password must not be blank")

        val createdUser = userService.create(sanitizedFirstName, sanitizedLastName, sanitizedEmail, sanitizedUsername, sanitizedPassword)

        return createdUser.toUserResponse()
    }

    @PostMapping("/login")
    fun authenticate(@Valid @RequestBody request: LoginRequest): AuthResponse {
        val sanitizedUsername = request.username.trimToNull() ?: throw IllegalArgumentException("Username must not be blank")
        val sanitizedPassword = request.password.trimToNull() ?: throw IllegalArgumentException("Password must not be blank")

        val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(sanitizedUsername, sanitizedPassword))

        SecurityContextHolder.getContext().authentication = authentication
        val userDetails = authentication.principal as UserDetailsImpl

        val jwt = jwtUtils.generateToken(userDetails.username ?: throw IllegalArgumentException("Username must not be blank"))

        return AuthResponse(token = jwt)
    }

    @PostMapping("/link/spotify")
    fun initiateLinkSpotify(@AuthenticationPrincipal userDetails: UserDetailsImpl): ResponseEntity<Void> {
        val state = idGeneratorService.uuid
        val authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("$spotifyAuthorizeUri?show_dialog=true")
            .clientId(spotifyClientId)
            .redirectUri(spotifyRedirectUri)
            .scopes(spotifyScope.split(",").toSet())
            .state(state.toString())
            .attributes(mapOf("registration_id" to "spotify"))
            .build()

        oAuth2LinkStateRepository.save(
            OAuth2LinkState(
                id = state,
                authorizationRequest = authorizationRequest,
                user = userDetails.user
            )
        )

        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", authorizationRequest.authorizationRequestUri)
            .build()
    }

}

data class RegistrationRequest(

    @field:NotBlank(message = "First Name must not be blank")
    @field:Size(min = 3, max = 24, message = "First Name must contain between 3 and 24 characters")
    val firstName: String,

    @field:NotBlank(message = "Last Name must not be blank")
    @field:Size(min = 3, max = 24, message = "Last Name must contain between 3 and 24 characters")
    val lastName: String,

    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 3, max = 24, message = "Username must contain between 3 and 24 characters")
    val username: String,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
        message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter"
    )
    val password: String

)

data class LoginRequest(

    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 3, max = 24, message = "Username must contain between 3 and 24 characters")
    val username: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
        message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter"
    )
    val password: String

)

data class UserResponse(val id: UUID, val firstName: String, val lastName: String, val email: String, val username: String)
data class AuthResponse(val token: String)

fun User.toUserResponse() = UserResponse(id = id, firstName = firstName, lastName = lastName, email = email, username = username)