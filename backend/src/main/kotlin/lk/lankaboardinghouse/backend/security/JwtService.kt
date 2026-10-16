package lk.lankaboardinghouse.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import lk.lankaboardinghouse.backend.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {

    init {
        require(secret.toByteArray().size >= 32) {
            "jwt.secret must be at least 32 bytes for HS256"
        }
    }

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(user: User): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("role", user.role.name)
            .claim("email", user.email)
            // Token version: bumped on password change or account disable.
            // JwtAuthFilter rejects any token whose "tv" doesn't match the user's.
            .claim("tv", user.tokenVersion)
            .issuedAt(Date(now))
            .expiration(Date(now + expirationMs))
            .signWith(key)
            .compact()
    }

    fun parseToken(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}