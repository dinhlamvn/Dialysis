package com.dialysis.app.ui.login

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.NetworkManager.asResult
import com.dialysis.app.data.network.request.LoginRequest
import com.dialysis.app.sharepref.AccountSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(
    private val accountSharePref: AccountSharePref
) : BaseViewModel<LoginState>(LoginState()) {

    val identifierState = flowOf(LoginState::identifier).collectStateUI("")
    val passwordState = flowOf(LoginState::password).collectStateUI("")
    val isPasswordVisibleState = flowOf(LoginState::isPasswordVisible).collectStateUI(false)
    val isLoginLoadingState = flowOf(LoginState::isLoginLoading).collectStateUI(false)
    val loginErrorState = flowOf(LoginState::loginError).collectStateUI(null)

    fun updateIdentifier(value: String) = setState { copy(identifier = value) }

    fun updatePassword(value: String) = setState { copy(password = value) }

    fun togglePasswordVisibility() = setState {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun login() {
        getState { state ->
            if (state.isLoginLoading) return@getState

            if (state.identifier.isBlank() || state.password.isBlank()) {
                setState { copy(loginError = "Please enter identifier and password") }
                return@getState
            }

            setState {
                copy(
                    isLoginLoading = true,
                    loginError = null,
                    isLoginSuccess = false
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                val request = LoginRequest(
                    identifier = state.identifier.trim(),
                    password = state.password
                )

                val result = NetworkManager.appPublicServices.login(request).asResult()

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: return@launch
                    accountSharePref.setToken(data.token)
                    accountSharePref.setTokenType(data.tokenType)
                    setState {
                        copy(
                            isLoginLoading = false,
                            loginError = null,
                            isLoginSuccess = true
                        )
                    }
                } else {
                    setState {
                        copy(
                            isLoginLoading = false,
                            loginError = result.exceptionOrNull()?.message,
                            isLoginSuccess = false
                        )
                    }
                }
            }
        }
    }
}
