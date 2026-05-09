package com.dialysis.app.ui.home.tabs

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val accountSharePref: AccountSharePref,
    private val userProfileSharePref: UserProfileSharePref,
    private val waterTrackingRepository: WaterTrackingRepository,
    private val weightTrackingRepository: WeightTrackingRepository,
    private val networkManager: NetworkManager
) : BaseViewModel<SettingsState>(SettingsState()) {

    val isLoadingAccountState = collectStateUI(SettingsState::isLoadingAccount)
    val accountContactState = collectStateUI(SettingsState::accountContact)
    val isLoggedInState = collectStateUI(SettingsState::isLoggedIn)
    val lastWaterSyncAtState = collectStateUI(SettingsState::lastWaterSyncAt)
    val signOutEventIdState = collectStateUI(SettingsState::signOutEventId)

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
        viewModelScope.launch(Dispatchers.IO) {
            accountSharePref.clear()
            userProfileSharePref.clear()
            waterTrackingRepository.clearAllLocalData()
            weightTrackingRepository.clearAll()
            setState {
                copy(
                    isLoadingAccount = false,
                    accountContact = null,
                    isLoggedIn = false,
                    lastWaterSyncAt = null,
                    signOutEventId = signOutEventId + 1
                )
            }
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
