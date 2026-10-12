package com.lk.lankaboardinghouse.android.data.remote

import com.lk.lankaboardinghouse.android.data.model.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface AccountApi {

    @PUT("api/account/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}