package org.danila.config

import org.danila.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.Instant

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationEntryPoint: AuthenticationEntryPoint
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
            auth.anyRequest().permitAll()
        }
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint = AuthenticationEntryPoint { request, response, authException ->
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.writer.write("""{"timestamp":"${Instant.now()}","message":"Unauthorized: Invalid or missing JWT token","errors":{}}""")
    }

}
