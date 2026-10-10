package com.lk.lankaboardinghouse.android.data.remote

import com.lk.lankaboardinghouse.android.data.model.BoardingHouseRequestDto
import com.lk.lankaboardinghouse.android.data.model.BoardingHouseResponseDto
import com.lk.lankaboardinghouse.android.data.model.DeclineRequest
import com.lk.lankaboardinghouse.android.data.model.DistrictDto
import com.lk.lankaboardinghouse.android.data.model.LoginRequest
import com.lk.lankaboardinghouse.android.data.model.RegisterRequest
import com.lk.lankaboardinghouse.android.data.model.TownDto
import com.lk.lankaboardinghouse.android.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UserResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @GET("api/districts")
    suspend fun getDistricts(): Response<List<DistrictDto>>

    @GET("api/towns")
    suspend fun getTowns(@Query("districtId") districtId: Long): Response<List<TownDto>>

    @POST("api/boarding-houses/request")
    suspend fun submitBoardingHouseRequest(@Body request: BoardingHouseRequestDto): Response<BoardingHouseResponseDto>

    @PUT("api/boarding-houses/{id}")
    suspend fun resubmitBoardingHouse(
        @Path("id") id: Long,
        @Body request: BoardingHouseRequestDto
    ): Response<BoardingHouseResponseDto>

    @GET("api/boarding-houses/owner/{ownerId}")
    suspend fun getOwnerListings(@Path("ownerId") ownerId: Long): Response<List<BoardingHouseResponseDto>>

    @GET("api/boarding-houses/search")
    suspend fun searchBoardingHouses(@Query("townId") townId: Long): Response<List<BoardingHouseResponseDto>>

    @GET("api/boarding-houses/pending")
    suspend fun getPendingBoardingHouses(): Response<List<BoardingHouseResponseDto>>

    @PUT("api/boarding-houses/{id}/approve")
    suspend fun approveBoardingHouse(@Path("id") id: Long): Response<BoardingHouseResponseDto>

    @PUT("api/boarding-houses/{id}/decline")
    suspend fun declineBoardingHouse(
        @Path("id") id: Long,
        @Body request: DeclineRequest
    ): Response<BoardingHouseResponseDto>
}