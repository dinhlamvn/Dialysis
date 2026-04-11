package com.dialysis.app.ui.home.tabs

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val accountSharePref: AccountSharePref,
    private val userProfileSharePref: UserProfileSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<SettingsState>(SettingsState()) {

    val isLoadingAccountState = collectStateUI(SettingsState::isLoadingAccount)
    val accountContactState = collectStateUI(SettingsState::accountContact)
    val isLoggedInState = collectStateUI(SettingsState::isLoggedIn)
    val dailyGoalMlState = collectStateUI(SettingsState::dailyGoalMl)

    init {
        setState {
            copy(
                isLoggedIn = accountSharePref.getToken().isNotBlank(),
                dailyGoalMl = userProfileSharePref.getDailyWaterGoalMl()
            )
        }
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
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
}
