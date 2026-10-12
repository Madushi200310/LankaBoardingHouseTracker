package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.ChangePasswordRequestDto
import lk.lankaboardinghouse.backend.repository.UserRepository
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

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_PASSWORD_BYTES = 72 // bcrypt ignores or rejects anything longer
    }

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
        if (dto.newPassword.length < MIN_PASSWORD_LENGTH) {
            throw IllegalArgumentException("New password must be at least $MIN_PASSWORD_LENGTH characters")
        }
        if (dto.newPassword.toByteArray(Charsets.UTF_8).size > MAX_PASSWORD_BYTES) {
            throw IllegalArgumentException("New password is too long")
        }
        if (dto.newPassword == dto.currentPassword) {
            throw IllegalArgumentException("New password must be different from the current one")
        }

        val encoded = passwordEncoder.encode(dto.newPassword) ?: error("Password encoding failed")
        userRepository.save(user.copy(password = encoded))

        return ResponseEntity.noContent().build()
    }
}