package com.dialysis.app.ui.home.tabs

import com.dialysis.app.base.BaseState

data class SettingsState(
    val isLoadingAccount: Boolean = false,
    val accountContact: String? = null,
    val isLoggedIn: Boolean = false,
    val dailyGoalMl: Int = 0
) : BaseState
