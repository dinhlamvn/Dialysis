package com.dialysis.app.ui.home.tabs

import androidx.annotation.StringRes
import com.dialysis.app.base.BaseState

data class SettingsState(
    val isLoadingAccount: Boolean = false,
    val accountContact: String? = null,
    val isLoggedIn: Boolean = false,
    val lastWaterSyncAt: Long? = null,
    val signOutEventId: Int = 0,
    val showDeleteAccountConfirm: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null,
    @param:StringRes val deleteAccountErrorResId: Int? = null
) : BaseState
