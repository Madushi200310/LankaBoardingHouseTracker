package com.lk.lankaboardinghouse.android.data.remote

import com.lk.lankaboardinghouse.android.data.model.LoginRequest
import com.lk.lankaboardinghouse.android.data.model.RegisterRequest
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UserResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>
}