package com.dialysis.app.ui.main

import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref

class IntroViewModel(
    private val accountSharePref: AccountSharePref,
    private val userProfileSharePref: UserProfileSharePref
) : BaseViewModel<IntroState>(IntroState()) {

    val shouldOpenHomeState = collectStateUI(IntroState::shouldOpenHome)

    fun startAppFlow() {
        getState { state ->
            if (state.shouldOpenHome) return@getState

            val hasToken = accountSharePref.getToken().isNotBlank()
            if (hasToken) {
                setState { copy(shouldOpenHome = true) }
                return@getState
            }

            val hasProfile = userProfileSharePref.getProfile() != null
            if (hasProfile) {
                setState { copy(shouldOpenHome = true) }
            }
        }
    }

    fun consumeOpenHomeEvent() = setState { copy(shouldOpenHome = false) }
}
