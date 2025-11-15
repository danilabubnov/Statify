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

enum class TokenType {
    ACCESS, REFRESH
}

@Component
class JwtUtils(
    @Value("\${jwt.access.secret.key}") private val jwtAccessSecretKey: String,
    @Value("\${jwt.refresh.secret.key}") private val jwtRefreshSecretKey: String,
    @Value("\${jwt.access.expiration.ms}") private val accessExpirationMs: Long,
    @Value("\${jwt.refresh.expiration.ms}") private val refreshExpirationMs: Long
) {

    private val keys = mapOf(
        TokenType.ACCESS to Keys.hmacShaKeyFor(jwtAccessSecretKey.toByteArray(StandardCharsets.UTF_8)),
        TokenType.REFRESH to Keys.hmacShaKeyFor(jwtRefreshSecretKey.toByteArray(StandardCharsets.UTF_8))
    )

    private val expirations = mapOf(
        TokenType.ACCESS to accessExpirationMs,
        TokenType.REFRESH to refreshExpirationMs
    )

    fun generateToken(userId: UUID, type: TokenType): String =
        generateToken(userId, expirations[type]!!, keys[type]!!)

    fun validateToken(token: String, type: TokenType): Boolean =
        try {
            verifyJwt(keys[type]!!).parse(token)
            true
        } catch (e: JwtException) {
            false
        }

    fun getUserIdFromToken(token: String, type: TokenType): UUID =
        UUID.fromString(
            verifyJwt(keys[type]!!)
                .parseSignedClaims(token)
                .payload
                .subject
        )

    private fun generateToken(userId: UUID, expiration: Long, key: SecretKey): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    private fun verifyJwt(key: SecretKey): JwtParser = Jwts
        .parser()
        .verifyWith(key)
        .build()

}