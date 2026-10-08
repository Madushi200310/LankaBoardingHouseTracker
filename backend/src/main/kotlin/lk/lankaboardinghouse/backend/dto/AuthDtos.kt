package lk.lankaboardinghouse.backend.dto

import lk.lankaboardinghouse.backend.model.Role

data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val role: Role
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class UserResponseDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: Role,
    val token: String
)