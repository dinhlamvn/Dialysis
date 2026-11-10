package com.dialysis.app.ui.home.tabs.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dialysis.app.data.network.response.LoginUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AccountProfileState {
    data object Idle : AccountProfileState
    data object Loading : AccountProfileState
    data class Loaded(val user: LoginUser) : AccountProfileState
    data class Error(val isNoInternet: Boolean) : AccountProfileState
    data object SessionExpired : AccountProfileState
}

/**
 * Read-only profile card of Settings > Account. [currentToken] is the stored token (blank when
 * logged out); it tells whether the user logged in again after a session-expired result.
 */
class AccountProfileViewModel(
    private val loader: AccountProfileLoader,
    private val currentToken: () -> String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _state = MutableStateFlow<AccountProfileState>(AccountProfileState.Idle)
    val state: StateFlow<AccountProfileState> = _state.asStateFlow()

    private val _showNoInternetDialog = MutableStateFlow(false)
    val showNoInternetDialog: StateFlow<Boolean> = _showNoInternetDialog.asStateFlow()

    /** Token in use when the session expired; a different non-blank token means a new login. */
    private var expiredToken: String? = null

    fun load() {
        if (_state.value == AccountProfileState.Loading) return
        _state.value = AccountProfileState.Loading
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { loader.load() }
            _state.value = when (result) {
                is ProfileLoadResult.Success -> AccountProfileState.Loaded(result.user)
                ProfileLoadResult.NoInternet -> {
                    _showNoInternetDialog.value = true
                    AccountProfileState.Error(isNoInternet = true)
                }
                ProfileLoadResult.SessionExpired -> {
                    expiredToken = currentToken()
                    AccountProfileState.SessionExpired
                }
                ProfileLoadResult.Failed -> AccountProfileState.Error(isNoInternet = false)
            }
        }
    }

    fun retry() = load()

    /** Reloads once when the user came back logged in again after the session expired. */
    fun onScreenResumed() {
        if (_state.value != AccountProfileState.SessionExpired) return
        val token = currentToken()
        if (token.isBlank() || token == expiredToken) return
        load()
    }

    fun dismissNoInternetDialog() {
        _showNoInternetDialog.value = false
    }
}
