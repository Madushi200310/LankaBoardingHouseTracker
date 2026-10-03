package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.LoginRequestDto
import lk.lankaboardinghouse.backend.dto.RegisterRequestDto
import lk.lankaboardinghouse.backend.dto.UserResponseDto
import lk.lankaboardinghouse.backend.model.User
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private fun toDto(u: User) = UserResponseDto(u.id, u.fullName, u.email, u.phoneNumber, u.role)

    @PostMapping("/register")
    fun register(@RequestBody dto: RegisterRequestDto): UserResponseDto {
        if (userRepository.findByEmail(dto.email) != null) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = User(
            fullName = dto.fullName,
            email = dto.email,
            password = passwordEncoder.encode(dto.password) ?: error("Password encoding failed"),
            phoneNumber = dto.phoneNumber,
            role = dto.role
        )

        return toDto(userRepository.save(user))
    }

    @PostMapping("/login")
    fun login(@RequestBody dto: LoginRequestDto): UserResponseDto {
        val user = userRepository.findByEmail(dto.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordEncoder.matches(dto.password, user.password)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return toDto(user)
    }
}