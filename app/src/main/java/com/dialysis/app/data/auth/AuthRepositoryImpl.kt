package com.dialysis.app.data.auth

import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.ChangePasswordRequest
import com.dialysis.app.data.network.request.ForgotPasswordRequest
import com.dialysis.app.domain.auth.AuthRepository
import com.dialysis.app.domain.auth.PasswordResetOtp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val networkManager: NetworkManager
) : AuthRepository {

    override suspend fun requestPasswordResetOtp(identifier: String): Result<PasswordResetOtp> {
        return withContext(Dispatchers.IO) {
            networkManager.resolve {
                networkManager.appPublicServices.forgotPassword(
                    ForgotPasswordRequest(identifier = identifier)
                )
            }.map { response ->
                PasswordResetOtp(
                    identifier = response.identifier,
                    identifierType = response.identifierType,
                    expiresIn = response.expiresIn?.toIntOrNull()
                )
            }
        }
    }

    override suspend fun changePassword(
        identifier: String,
        otpCode: String,
        newPassword: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            networkManager.resolveNullable {
                networkManager.appPublicServices.changePassword(
                    ChangePasswordRequest(
                        identifier = identifier,
                        otpCode = otpCode,
                        newPassword = newPassword
                    )
                )
            }.map { Unit }
        }
    }
}
