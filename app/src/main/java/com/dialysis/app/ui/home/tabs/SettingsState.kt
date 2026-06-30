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
    @param:StringRes val deleteAccountErrorResId: Int? = null,
    val urineSamplesMode: UrineSamplesMode = UrineSamplesMode.Main,
    val urineAmountInput: String = "",
    val urineNoteInput: String = "",
    val isSavingUrineSample: Boolean = false,
    val urineSaveSuccess: Boolean = false,
    val urineSaveError: String? = null,
    @param:StringRes val urineSaveErrorResId: Int? = null,
    val isLoadingUrineSamples: Boolean = false,
    val urineSamples: List<UrineSampleUiModel> = emptyList(),
    val urineSamplesError: String? = null,
    @param:StringRes val urineSamplesErrorResId: Int? = null
) : BaseState

enum class UrineSamplesMode {
    Main,
    AddToday,
    History
}

data class UrineSampleUiModel(
    val id: Long,
    val amountMl: Int,
    val sampleTimeMillis: Long?,
    val note: String?
)
