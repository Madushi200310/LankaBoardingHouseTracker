package com.lk.lankaboardinghouse.android.data.remote

import com.lk.lankaboardinghouse.android.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 = special alias the Android Emulator uses to reach your PC's localhost
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = SessionManager.currentUser?.token

        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        // A 401 on a non-login call while we hold a token means it expired or is invalid
        val isAuthCall = original.url.encodedPath.startsWith("/api/auth")
        if (response.code == 401 && !isAuthCall && token != null) {
            SessionManager.sessionExpired = true
        }

        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}