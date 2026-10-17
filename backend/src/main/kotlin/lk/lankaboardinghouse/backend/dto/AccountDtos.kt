package lk.lankaboardinghouse.backend.dto

data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)