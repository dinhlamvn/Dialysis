package com.dialysis.app.ui.login

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.LoginRequest
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.sync.WaterIntakeSyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager,
    private val waterIntakeSyncScheduler: WaterIntakeSyncScheduler,
    private val userProfileSharePref: UserProfileSharePref
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
                    data.user.initialWeight?.toFloat()?.takeIf { it > 0f }?.let {
                        userProfileSharePref.saveInitialWeightKg(it)
                    }
                    waterIntakeSyncScheduler.enqueue()
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
                            loginError = result.exceptionOrNull()?.message.toLoginErrorMessage(),
                            isLoginSuccess = false
                        )
                    }
                }
            }
        }
    }
}

private fun String?.toLoginErrorMessage(): String {
    val message = this.orEmpty()
    val normalized = message.lowercase()
    return when {
        normalized.contains("401") ||
            normalized.contains("unauthenticated") ||
            normalized.contains("unauthorized") -> LOGIN_INVALID_CREDENTIALS_MESSAGE
        message.isBlank() -> LOGIN_FALLBACK_ERROR_MESSAGE
        else -> message
    }
}

private const val LOGIN_INVALID_CREDENTIALS_MESSAGE =
    "Tài khoản hoặc mật khẩu không đúng. Vui lòng kiểm tra lại."
private const val LOGIN_FALLBACK_ERROR_MESSAGE =
    "Không thể đăng nhập. Vui lòng thử lại."
