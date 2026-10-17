package lk.lankaboardinghouse.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val claims = jwtService.parseToken(token)
                val userId = claims.subject.toLongOrNull()
                val role = claims["role"] as? String
                val tokenVersion = (claims["tv"] as? Number)?.toLong()

                if (userId == null || role == null || tokenVersion == null) {
                    SecurityContextHolder.clearContext()
                } else {
                    val user = userRepository.findById(userId).orElse(null)

                    // Reject if: user gone, account disabled, or password changed
                    // since the token was issued.
                    if (user == null || user.disabled || user.tokenVersion != tokenVersion) {
                        SecurityContextHolder.clearContext()
                    } else {
                        val authentication = UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            listOf(SimpleGrantedAuthority("ROLE_$role"))
                        )
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            } catch (_: Exception) {
                // Invalid or expired token: leave the request unauthenticated
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }
}