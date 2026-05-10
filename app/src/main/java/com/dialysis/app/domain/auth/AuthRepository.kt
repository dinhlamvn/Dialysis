package com.dialysis.app.domain.auth

interface AuthRepository {
    suspend fun requestPasswordResetOtp(identifier: String): Result<PasswordResetOtp>
    suspend fun changePassword(identifier: String, otpCode: String, newPassword: String): Result<Unit>
}
