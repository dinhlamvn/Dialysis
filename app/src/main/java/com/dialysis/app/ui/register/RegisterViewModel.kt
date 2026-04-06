package com.dialysis.app.ui.register

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.NetworkManager.asResult
import com.dialysis.app.data.network.request.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel : BaseViewModel<RegisterState>(RegisterState()) {

    val emailOrPhoneState = flowOf(RegisterState::emailOrPhone).collectStateUI("")
    val nameState = flowOf(RegisterState::name).collectStateUI("")
    val passwordState = flowOf(RegisterState::password).collectStateUI("")
    val confirmPasswordState = flowOf(RegisterState::confirmPassword).collectStateUI("")
    val isRegisterLoadingState = flowOf(RegisterState::isRegisterLoading).collectStateUI(false)
    val registerErrorState = flowOf(RegisterState::registerError).collectStateUI(null)

    fun updateEmailOrPhone(value: String) = setState { copy(emailOrPhone = value) }

    fun updateName(value: String) = setState { copy(name = value) }

    fun updatePassword(value: String) = setState { copy(password = value) }

    fun updateConfirmPassword(value: String) = setState { copy(confirmPassword = value) }

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
                    username = emailOrPhone,
                    email = if (isEmail) emailOrPhone else "",
                    phone = if (isEmail) "" else emailOrPhone,
                    password = state.password,
                    passwordConfirmation = state.confirmPassword,
                    name = state.name.trim(),
                )

                val result = NetworkManager.appPublicServices.register(request).asResult()
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
