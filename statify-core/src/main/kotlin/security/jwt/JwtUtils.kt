package org.danila.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(
    @Value("\${jwt.access.secret.key}") private val jwtAccessSecretKey: String,
    @Value("\${jwt.refresh.secret.key}") private val jwtRefreshSecretKey: String,
    @Value("\${jwt.access.expiration.ms}") private val accessExpirationMs: Long,
    @Value("\${jwt.refresh.expiration.ms}") private val refreshExpirationMs: Long
) {

    private val accessKey = Keys.hmacShaKeyFor(jwtAccessSecretKey.toByteArray(StandardCharsets.UTF_8))
    private val refreshKey = Keys.hmacShaKeyFor(jwtRefreshSecretKey.toByteArray(StandardCharsets.UTF_8))

    // -------------------- ACCESS --------------------

    fun generateAccessToken(username: String): String =
        generateToken(username, accessExpirationMs, accessKey)

    // -------------------- REFRESH --------------------

    fun generateRefreshToken(username: String): String =
        generateToken(username, refreshExpirationMs, refreshKey)

    fun validateRefreshToken(token: String): Boolean =
        validateToken(token, refreshKey)

    fun getUsernameFromRefreshToken(token: String): String =
        getUsernameFromToken(token, refreshKey)

    // -------------------- COMMON --------------------

    private fun generateToken(username: String, expiration: Long, key: SecretKey): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUsernameFromToken(token: String, key: SecretKey = accessKey): String =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun validateToken(token: String, key: SecretKey = accessKey): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parse(token)
            true
        } catch (e: JwtException) {
            false
        }
    }

}