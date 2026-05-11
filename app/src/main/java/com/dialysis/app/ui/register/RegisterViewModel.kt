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

    val usernameState = collectStateUI(RegisterState::username)
    val emailState = collectStateUI(RegisterState::email)
    val phoneState = collectStateUI(RegisterState::phone)
    val nameState = collectStateUI(RegisterState::name)
    val passwordState = collectStateUI(RegisterState::password)
    val confirmPasswordState = collectStateUI(RegisterState::confirmPassword)
    val isPasswordVisibleState = collectStateUI(RegisterState::isPasswordVisible)
    val isConfirmPasswordVisibleState =
        collectStateUI(RegisterState::isConfirmPasswordVisible)
    val isRegisterLoadingState = collectStateUI(RegisterState::isRegisterLoading)
    val registerErrorState = collectStateUI(RegisterState::registerError)

    fun updateUsername(value: String) = setState { copy(username = value) }

    fun updateEmail(value: String) = setState { copy(email = value) }

    fun updatePhone(value: String) = setState { copy(phone = value) }

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
                val username = state.username.trim()
                val email = state.email.trim()
                val phone = state.phone.trim()
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    phone = phone,
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
