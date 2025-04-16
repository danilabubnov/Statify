package org.danila.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class JwtUtils(
    @Value("\${jwt.secret.key}") private val jwtSecretKey: String,
    @Value("\${jwt.expiration.ms}") private val jwtExpirationMs: Long
) {

    private val key = Keys.hmacShaKeyFor(jwtSecretKey.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(username: String): String {
        val date = Date()
        val expiryDate = Date(date.time + jwtExpirationMs)

        return Jwts.builder()
            .subject(username)
            .issuedAt(date)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUsernameFromToken(token: String): String = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload
        .subject

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parse(token)

            return true
        } catch (e: JwtException) {
            return false
        }
    }

}