package com.lk.lankaboardinghouse.android.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val role: String
)

data class UserResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String
)