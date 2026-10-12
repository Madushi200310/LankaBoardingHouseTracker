package com.lk.lankaboardinghouse.android.data.model

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)