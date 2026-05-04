package com.dialysis.app.ui.home.tabs

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<SettingsState>(SettingsState()) {

    val isLoadingAccountState = collectStateUI(SettingsState::isLoadingAccount)
    val accountContactState = collectStateUI(SettingsState::accountContact)
    val isLoggedInState = collectStateUI(SettingsState::isLoggedIn)
    val lastWaterSyncAtState = collectStateUI(SettingsState::lastWaterSyncAt)

    init {
        setState {
            copy(
                isLoggedIn = accountSharePref.getToken().isNotBlank(),
                lastWaterSyncAt = accountSharePref.getLastWaterSyncAt()
            )
        }
        observeLastWaterSyncAt()
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        if (accountSharePref.getToken().isBlank()) {
            setState {
                copy(
                    isLoadingAccount = false,
                    accountContact = null,
                    isLoggedIn = false
                )
            }
            return
        }

        setState { copy(isLoadingAccount = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = networkManager.resolve { networkManager.appServices.me() }
            val accountContact = result.getOrNull()?.let { user ->
                user.phone?.takeIf { it.isNotBlank() }
                    ?: user.email?.takeIf { it.isNotBlank() }
                    ?: user.username.takeIf { it.isNotBlank() }
            }
            setState {
                copy(
                    isLoadingAccount = false,
                    accountContact = accountContact,
                    isLoggedIn = accountSharePref.getToken().isNotBlank()
                )
            }
        }
    }

    fun signOut() {
        accountSharePref.clear()
        setState {
            copy(
                isLoadingAccount = false,
                accountContact = null,
                isLoggedIn = false,
                lastWaterSyncAt = null
            )
        }
    }

    private fun observeLastWaterSyncAt() {
        viewModelScope.launch {
            accountSharePref.observeLastWaterSyncAt().collectLatest { timestamp ->
                setState { copy(lastWaterSyncAt = timestamp) }
            }
        }
    }
}
