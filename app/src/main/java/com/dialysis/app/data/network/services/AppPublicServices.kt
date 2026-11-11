package com.dialysis.app.data.network.services

import com.dialysis.app.data.network.request.ChangePasswordRequest
import com.dialysis.app.data.network.request.ForgotPasswordRequest
import com.dialysis.app.data.network.request.LoginRequest
import com.dialysis.app.data.network.request.RegisterRequest
import com.dialysis.app.data.network.request.VerifyOtpRequest
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.ForgotPasswordResponse
import com.dialysis.app.data.network.response.LoginResponse
import com.dialysis.app.data.network.response.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AppPublicServices {
    @POST("mobile/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    /** Blocking login, for the sync client's 401 authenticator (runs on an OkHttp thread). */
    @POST("mobile/auth/login")
    fun loginCall(
        @Body request: LoginRequest
    ): Call<ApiResponse<LoginResponse>>

    @POST("mobile/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<RegisterResponse>

    @POST("mobile/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): ApiResponse<LoginResponse>

    @POST("mobile/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): ApiResponse<ForgotPasswordResponse>

    @POST("mobile/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): ApiResponse<Map<String, Any>?>

}
