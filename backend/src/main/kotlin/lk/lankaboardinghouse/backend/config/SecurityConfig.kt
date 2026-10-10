package lk.lankaboardinghouse.backend.config

import lk.lankaboardinghouse.backend.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // Logged in, but wrong role: answer 403 directly (no forward to /error)
        val forbiddenHandler = AccessDeniedHandler { _, response, _ ->
            response.status = HttpStatus.FORBIDDEN.value()
        }

        http {
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                accessDeniedHandler = forbiddenHandler
            }
            authorizeHttpRequests {
                authorize("/error", permitAll)

                // Public
                authorize("/api/auth/**", permitAll)
                authorize(HttpMethod.GET, "/api/districts", permitAll)
                authorize(HttpMethod.GET, "/api/towns", permitAll)

                // Any logged-in user can search
                authorize(HttpMethod.GET, "/api/boarding-houses/search", authenticated)

                // Owner only
                authorize(HttpMethod.POST, "/api/boarding-houses/request", hasRole("OWNER"))
                authorize(HttpMethod.GET, "/api/boarding-houses/owner/*", hasRole("OWNER"))
                authorize(HttpMethod.PUT, "/api/boarding-houses/*", hasRole("OWNER"))

                // Admin only
                authorize(HttpMethod.GET, "/api/boarding-houses/pending", hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/api/boarding-houses/*/approve", hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/api/boarding-houses/*/decline", hasRole("ADMIN"))
                authorize(HttpMethod.GET, "/api/boarding-houses", hasRole("ADMIN"))

                authorize(anyRequest, authenticated)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthFilter)
        }
        return http.build()
    }
}