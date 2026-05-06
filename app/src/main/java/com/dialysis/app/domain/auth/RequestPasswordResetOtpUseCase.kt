package com.dialysis.app.domain.auth

class RequestPasswordResetOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identifier: String): Result<PasswordResetOtp> {
        return authRepository.requestPasswordResetOtp(identifier.trim())
    }
}
