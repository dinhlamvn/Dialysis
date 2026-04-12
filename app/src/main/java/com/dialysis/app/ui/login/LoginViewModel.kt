package com.dialysis.app.ui.login

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.LoginRequest
import com.dialysis.app.sharepref.AccountSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<LoginState>(LoginState()) {

    val identifierState = collectStateUI(LoginState::identifier)
    val passwordState = collectStateUI(LoginState::password)
    val isPasswordVisibleState = collectStateUI(LoginState::isPasswordVisible)
    val isLoginLoadingState = collectStateUI(LoginState::isLoginLoading)
    val loginErrorState = collectStateUI(LoginState::loginError)

    fun updateIdentifier(value: String) = setState { copy(identifier = value) }

    fun updatePassword(value: String) = setState { copy(password = value) }

    fun togglePasswordVisibility() = setState {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun login() {
        getState { state ->
            if (state.isLoginLoading) return@getState

            if (state.identifier.isBlank() || state.password.isBlank()) {
                setState { copy(loginError = "Vui lòng nhập tài khoản và mật khẩu") }
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

                val result = networkManager.resolve {
                    networkManager.appPublicServices.login(request)
                }

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
