package com.dialysis.app.domain.auth

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, otpCode: String, newPassword: String): Result<Unit> {
        return authRepository.changePassword(
            identifier = identifier.trim(),
            otpCode = otpCode.trim(),
            newPassword = newPassword
        )
    }
}
