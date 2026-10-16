package com.lk.lankaboardinghouse.android.data.model

data class AdminUserDto(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val disabled: Boolean
)

data class SetDisabledRequest(
    val disabled: Boolean
)