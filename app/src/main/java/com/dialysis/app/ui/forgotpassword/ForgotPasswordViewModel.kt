package com.dialysis.app.ui.forgotpassword

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.domain.auth.AuthInputValidator
import com.dialysis.app.domain.auth.RequestPasswordResetOtpUseCase
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val requestPasswordResetOtp: RequestPasswordResetOtpUseCase
) : BaseViewModel<ForgotPasswordState>(ForgotPasswordState()) {

    val identifierState = collectStateUI(ForgotPasswordState::identifier)
    val isLoadingState = collectStateUI(ForgotPasswordState::isLoading)
    val errorMessageState = collectStateUI(ForgotPasswordState::errorMessage)

    fun updateIdentifier(value: String) = setState {
        copy(identifier = value, errorMessage = null, isOtpSent = false)
    }

    fun requestOtp() {
        getState { state ->
            if (state.isLoading) return@getState
            val trimmed = state.identifier.trim()
            val validationError = AuthInputValidator.identifierError(trimmed)
            if (validationError != null) {
                setState { copy(errorMessage = validationError) }
                return@getState
            }

            setState { copy(isLoading = true, errorMessage = null, isOtpSent = false) }
            viewModelScope.launch {
                val result = requestPasswordResetOtp(trimmed)
                val data = result.getOrNull()
                setState {
                    if (result.isSuccess && data != null) {
                        copy(
                            isLoading = false,
                            successIdentifier = data.identifier,
                            successIdentifierType = data.identifierType,
                            isOtpSent = true
                        )
                    } else {
                        copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Không thể gửi mã OTP"
                        )
                    }
                }
            }
        }
    }
}
