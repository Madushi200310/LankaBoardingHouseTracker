package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.ChangePasswordRequestDto
import lk.lankaboardinghouse.backend.repository.UserRepository
import lk.lankaboardinghouse.backend.security.PasswordPolicy
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // Any logged-in user (admin, owner or user) can change their own password.
    // SecurityConfig's "anyRequest authenticated" rule already protects this path.
    @PutMapping("/password")
    fun changePassword(
        @RequestBody dto: ChangePasswordRequestDto,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val userId = authentication.name.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid session")
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // 400 on purpose: a 401 would make the app think the session expired and log the user out
        if (!passwordEncoder.matches(dto.currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Same rule as register and admin seed
        PasswordPolicy.validate(dto.newPassword)

        if (dto.newPassword == dto.currentPassword) {
            throw IllegalArgumentException("New password must be different from the current one")
        }

        val encoded = passwordEncoder.encode(dto.newPassword) ?: error("Password encoding failed")

        // Bump tokenVersion → every previously-issued token becomes invalid.
        // JwtAuthFilter compares the token's "tv" against this value.
        userRepository.save(
            user.copy(
                password = encoded,
                tokenVersion = user.tokenVersion + 1
            )
        )

        return ResponseEntity.noContent().build()
    }
}