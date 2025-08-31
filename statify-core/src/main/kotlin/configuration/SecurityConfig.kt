package org.danila.configuration

import org.danila.security.PublicEndpoints
import org.danila.security.jwt.JwtAuthenticationFilter
import org.danila.security.jwt.JwtUtils
import org.danila.security.oauth2.CustomOAuth2AuthorizationRequestRepository
import org.danila.security.oauth2.OAuth2SuccessHandler
import org.danila.security.oauth2.SpotifyOAuth2UserService
import org.danila.security.user.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.Instant

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Lazy private val customAuthorizationRequestRepository: CustomOAuth2AuthorizationRequestRepository,
    @Lazy private val spotifyOAuth2UserService: SpotifyOAuth2UserService,
    @Lazy private val userDetailsServiceImpl: UserDetailsServiceImpl,
    @Lazy private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    @Lazy private val publicEndpoints: PublicEndpoints,
    @Lazy private val jwtUtils: JwtUtils
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationEntryPoint: AuthenticationEntryPoint,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain = http
        .csrf { csrf ->
            csrf.disable()
        }
        .cors { }
        .sessionManagement { session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        .exceptionHandling { exception ->
            exception.authenticationEntryPoint(authenticationEntryPoint)
        }
        .authorizeHttpRequests { auth ->
            auth
                .requestMatchers(*publicEndpoints.publicEndpoints.toTypedArray()).permitAll()
                .anyRequest().authenticated()
        }
        .oauth2Login { oauth2 ->
            oauth2
                .authorizationEndpoint { endpoint ->
                    endpoint.authorizationRequestRepository(customAuthorizationRequestRepository)
                }
                .userInfoEndpoint { userInfo -> userInfo.userService(spotifyOAuth2UserService) }
                .successHandler(oAuth2SuccessHandler)
                .failureHandler { request, response, exception ->
                    response.status = HttpStatus.UNAUTHORIZED.value()
                    response.contentType = "application/json"
                    response.writer.write(
                        """{"timestamp":"${Instant.now()}","message":"OAuth2 Error: ${exception.message}"}"""
                    )
                }
        }
        .userDetailsService(userDetailsServiceImpl)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun jwtAuthenticationFilter() = JwtAuthenticationFilter(userDetailsServiceImpl, publicEndpoints, jwtUtils)

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager = authenticationConfiguration.authenticationManager

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint = AuthenticationEntryPoint { request, response, authException ->
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.writer.write("""{"timestamp":"${Instant.now()}","message":"Invalid email or password","errors":{}}""")
    }

}