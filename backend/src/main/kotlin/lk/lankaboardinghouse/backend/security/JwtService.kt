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

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(user: User): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("role", user.role.name)
            .claim("email", user.email)
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