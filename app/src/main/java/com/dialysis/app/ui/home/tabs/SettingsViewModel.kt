package com.dialysis.app.ui.home.tabs

import androidx.lifecycle.viewModelScope
import com.dialysis.app.R
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

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
    val showDeleteAccountConfirmState = collectStateUI(SettingsState::showDeleteAccountConfirm)
    val isDeletingAccountState = collectStateUI(SettingsState::isDeletingAccount)
    val deleteAccountErrorState = collectStateUI(SettingsState::deleteAccountError)
    val deleteAccountErrorResIdState = collectStateUI(SettingsState::deleteAccountErrorResId)

    private val deleteAccountRequestInFlight = AtomicBoolean(false)

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
            clearLocalAccountAndNotify()
        }
    }

    fun requestDeleteAccount() = setState {
        copy(
            showDeleteAccountConfirm = true,
            deleteAccountError = null,
            deleteAccountErrorResId = null
        )
    }

    fun dismissDeleteAccountConfirm() = setState {
        if (isDeletingAccount) {
            this
        } else {
            copy(showDeleteAccountConfirm = false)
        }
    }

    fun clearDeleteAccountError() = setState {
        copy(deleteAccountError = null, deleteAccountErrorResId = null)
    }

    fun confirmDeleteAccount() {
        getState { state ->
            if (state.isDeletingAccount) return@getState
            if (accountSharePref.getToken().isBlank()) {
                setState {
                    copy(
                        showDeleteAccountConfirm = false,
                        isDeletingAccount = false,
                        deleteAccountError = null,
                        deleteAccountErrorResId = R.string.settings_delete_account_invalid_token
                    )
                }
                return@getState
            }
            if (!deleteAccountRequestInFlight.compareAndSet(false, true)) return@getState
            setState {
                copy(
                    isDeletingAccount = true,
                    deleteAccountError = null,
                    deleteAccountErrorResId = null
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = networkManager.resolveNullable {
                        networkManager.appServices.deleteAccount()
                    }
                    if (result.isSuccess) {
                        clearLocalAccountAndNotify(
                            showDeleteAccountConfirm = false,
                            isDeletingAccount = false
                        )
                    } else {
                        setState {
                            copy(
                                showDeleteAccountConfirm = false,
                                isDeletingAccount = false,
                                deleteAccountError = result.exceptionOrNull()?.message,
                                deleteAccountErrorResId = R.string.settings_delete_account_failed_message
                            )
                        }
                    }
                } finally {
                    deleteAccountRequestInFlight.set(false)
                    getState { currentState ->
                        if (currentState.isDeletingAccount) {
                            setState { copy(isDeletingAccount = false) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun clearLocalAccountAndNotify(
        showDeleteAccountConfirm: Boolean = false,
        isDeletingAccount: Boolean = false
    ) {
        runCatching { accountSharePref.clear() }
        runCatching { userProfileSharePref.clear() }
        runCatching { waterTrackingRepository.clearAllLocalData() }
        runCatching { weightTrackingRepository.clearAll() }
        setState {
            copy(
                isLoadingAccount = false,
                accountContact = null,
                isLoggedIn = false,
                lastWaterSyncAt = null,
                showDeleteAccountConfirm = showDeleteAccountConfirm,
                isDeletingAccount = isDeletingAccount,
                deleteAccountError = null,
                deleteAccountErrorResId = null,
                signOutEventId = signOutEventId + 1
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
