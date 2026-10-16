package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.AdminUserResponseDto
import lk.lankaboardinghouse.backend.dto.SetDisabledRequestDto
import lk.lankaboardinghouse.backend.model.Role
import lk.lankaboardinghouse.backend.model.User
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val userRepository: UserRepository
) {

    private fun toDto(u: User) = AdminUserResponseDto(
        id = u.id,
        fullName = u.fullName,
        email = u.email,
        phoneNumber = u.phoneNumber,
        role = u.role,
        disabled = u.disabled
    )

    // Lists every user, grouped by role (ADMIN → OWNER → USER) then by name.
    @GetMapping
    fun listAll(): List<AdminUserResponseDto> =
        userRepository.findAll()
            .sortedWith(compareBy({ it.role }, { it.fullName }))
            .map { toDto(it) }

    // Enables or disables a non-admin account.
    @PutMapping("/{id}/disabled")
    fun setDisabled(
        @PathVariable id: Long,
        @RequestBody dto: SetDisabledRequestDto,
        authentication: Authentication
    ): AdminUserResponseDto {
        val currentAdminId = authentication.name.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid session")

        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Guard rails: an admin can't lock themselves or another admin out.
        if (user.id == currentAdminId) {
            throw IllegalArgumentException("You cannot disable your own account")
        }
        if (user.role == Role.ADMIN) {
            throw IllegalArgumentException("Admin accounts cannot be disabled")
        }

        // No-op if the state already matches what was requested.
        if (user.disabled == dto.disabled) {
            return toDto(user)
        }

        // Disabling bumps tokenVersion so any tokens the user currently holds
        // are rejected on their next request (belt-and-braces on top of the
        // disabled check already in JwtAuthFilter).
        val updated = if (dto.disabled) {
            user.copy(disabled = true, tokenVersion = user.tokenVersion + 1)
        } else {
            user.copy(disabled = false)
        }

        return toDto(userRepository.save(updated))
    }
}