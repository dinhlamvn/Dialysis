package com.dialysis.app.ui.changepassword

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.domain.auth.AuthInputValidator
import com.dialysis.app.domain.auth.ChangePasswordUseCase
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase
) : BaseViewModel<ChangePasswordState>(ChangePasswordState()) {

    val newPasswordState = collectStateUI(ChangePasswordState::newPassword)
    val confirmPasswordState = collectStateUI(ChangePasswordState::confirmPassword)
    val isPasswordVisibleState = collectStateUI(ChangePasswordState::isPasswordVisible)
    val isConfirmPasswordVisibleState = collectStateUI(ChangePasswordState::isConfirmPasswordVisible)
    val isLoadingState = collectStateUI(ChangePasswordState::isLoading)
    val errorMessageState = collectStateUI(ChangePasswordState::errorMessage)

    fun setResetData(identifier: String, otpCode: String) = setState {
        copy(identifier = identifier, otpCode = otpCode)
    }

    fun updateNewPassword(value: String) = setState {
        copy(newPassword = value, errorMessage = null, isSuccess = false)
    }

    fun updateConfirmPassword(value: String) = setState {
        copy(confirmPassword = value, errorMessage = null, isSuccess = false)
    }

    fun togglePasswordVisibility() = setState {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() = setState {
        copy(isConfirmPasswordVisible = !isConfirmPasswordVisible)
    }

    fun changePassword() {
        getState { state ->
            if (state.isLoading) return@getState
            AuthInputValidator.passwordError(state.newPassword)?.let {
                setState { copy(errorMessage = it) }
                return@getState
            }
            if (state.newPassword != state.confirmPassword) {
                setState { copy(errorMessage = "Mật khẩu xác nhận không khớp") }
                return@getState
            }
            if (state.identifier.isBlank() || state.otpCode.length != 6) {
                setState { copy(errorMessage = "Thiếu thông tin xác minh OTP") }
                return@getState
            }

            setState { copy(isLoading = true, errorMessage = null, isSuccess = false) }
            viewModelScope.launch {
                val result = changePasswordUseCase(
                    identifier = state.identifier,
                    otpCode = state.otpCode,
                    newPassword = state.newPassword
                )
                setState {
                    if (result.isSuccess) {
                        copy(isLoading = false, isSuccess = true)
                    } else {
                        copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Không thể đổi mật khẩu"
                        )
                    }
                }
            }
        }
    }
}
