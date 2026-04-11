package com.dialysis.app.ui.register

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val networkManager: NetworkManager
) : BaseViewModel<RegisterState>(RegisterState()) {

    val emailOrPhoneState = collectStateUI(RegisterState::emailOrPhone)
    val nameState = collectStateUI(RegisterState::name)
    val passwordState = collectStateUI(RegisterState::password)
    val confirmPasswordState = collectStateUI(RegisterState::confirmPassword)
    val isPasswordVisibleState = collectStateUI(RegisterState::isPasswordVisible)
    val isConfirmPasswordVisibleState =
        collectStateUI(RegisterState::isConfirmPasswordVisible)
    val isRegisterLoadingState = collectStateUI(RegisterState::isRegisterLoading)
    val registerErrorState = collectStateUI(RegisterState::registerError)

    fun updateEmailOrPhone(value: String) = setState { copy(emailOrPhone = value) }

    fun updateName(value: String) = setState { copy(name = value) }

    fun updatePassword(value: String) = setState { copy(password = value) }

    fun updateConfirmPassword(value: String) = setState { copy(confirmPassword = value) }

    fun togglePasswordVisibility() = setState {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() = setState {
        copy(isConfirmPasswordVisible = !isConfirmPasswordVisible)
    }

    fun register() {
        getState { state ->
            if (state.isRegisterLoading) return@getState

            setState {
                copy(
                    isRegisterLoading = true,
                    registerError = null,
                    isRegisterSuccess = false
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                val emailOrPhone = state.emailOrPhone.trim()
                val isEmail = emailOrPhone.contains("@")
                val request = RegisterRequest(
                    username = emailOrPhone.replaceAfter("@", "").replace("@", ""),
                    email = if (isEmail) emailOrPhone else "",
                    phone = if (isEmail) "" else emailOrPhone,
                    password = state.password,
                    passwordConfirmation = state.confirmPassword,
                    name = state.name.trim(),
                )

                val result = networkManager.resolve {
                    networkManager.appPublicServices.register(request)
                }
                if (result.isSuccess) {
                    setState {
                        copy(
                            isRegisterLoading = false,
                            registerError = null,
                            isRegisterSuccess = true
                        )
                    }
                } else {
                    setState {
                        copy(
                            isRegisterLoading = false,
                            registerError = result.exceptionOrNull()?.message,
                            isRegisterSuccess = false
                        )
                    }
                }
            }
        }
    }
}
