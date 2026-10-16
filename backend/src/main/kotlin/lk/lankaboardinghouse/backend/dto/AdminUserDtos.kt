package lk.lankaboardinghouse.backend.dto

import lk.lankaboardinghouse.backend.model.Role

data class AdminUserResponseDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: Role,
    val disabled: Boolean
)

data class SetDisabledRequestDto(
    val disabled: Boolean
)