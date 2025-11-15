package org.danila.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(
    @Value("\${jwt.access.secret.key}") private val jwtAccessSecretKey: String
) {

    private val accessKey: SecretKey = Keys.hmacShaKeyFor(jwtAccessSecretKey.toByteArray(StandardCharsets.UTF_8))

    fun validateToken(token: String): Boolean =
        try {
            verifyJwt(accessKey).parse(token)
            true
        } catch (e: JwtException) {
            false
        }

    fun getUserIdFromToken(token: String): UUID =
        UUID.fromString(
            verifyJwt(accessKey)
                .parseSignedClaims(token)
                .payload
                .subject
        )

    private fun verifyJwt(key: SecretKey): JwtParser = Jwts
        .parser()
        .verifyWith(key)
        .build()

}
