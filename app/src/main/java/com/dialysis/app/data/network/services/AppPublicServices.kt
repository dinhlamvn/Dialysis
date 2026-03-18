package com.dialysis.app.data.network.services

import com.dialysis.app.data.network.request.LoginRequest
import com.dialysis.app.data.network.request.RegisterRequest
import com.dialysis.app.data.network.request.VerifyOtpRequest
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.LoginResponse
import com.dialysis.app.data.network.response.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AppPublicServices {
    @POST("mobile/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    @POST("mobile/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<RegisterResponse>

    @POST("mobile/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): ApiResponse<LoginResponse>
}
