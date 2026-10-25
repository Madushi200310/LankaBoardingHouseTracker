package com.lk.lankaboardinghouse.android.data.remote

import com.lk.lankaboardinghouse.android.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

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

    @PUT("api/boarding-houses/{id}/availability")
    suspend fun setAvailability(
        @Path("id") id: Long,
        @Body request: AvailabilityRequest
    ): Response<BoardingHouseResponseDto>

    @DELETE("api/boarding-houses/{id}")
    suspend fun deleteBoardingHouse(@Path("id") id: Long): Response<Unit>

    @GET("api/boarding-houses/owner/{ownerId}")
    suspend fun getOwnerListings(@Path("ownerId") ownerId: Long): Response<List<BoardingHouseResponseDto>>

    @GET("api/boarding-houses/search")
    suspend fun searchBoardingHouses(
        @Query("townId") townId: Long,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sort") sort: String? = null
    ): Response<List<BoardingHouseResponseDto>>

    @GET("api/boarding-houses/pending")
    suspend fun getPendingBoardingHouses(): Response<List<BoardingHouseResponseDto>>

    @PUT("api/boarding-houses/{id}/approve")
    suspend fun approveBoardingHouse(@Path("id") id: Long): Response<BoardingHouseResponseDto>

    @PUT("api/boarding-houses/{id}/decline")
    suspend fun declineBoardingHouse(
        @Path("id") id: Long,
        @Body request: DeclineRequest
    ): Response<BoardingHouseResponseDto>

    // --- NEW: Upload Photos Endpoint ---
    @Multipart
    @POST("api/boarding-houses/{id}/photos")
    suspend fun uploadPhotos(
        @Path("id") id: Long,
        @Part files: List<MultipartBody.Part>
    ): Response<BoardingHouseResponseDto>
}