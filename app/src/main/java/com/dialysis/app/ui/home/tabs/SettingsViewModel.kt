package com.dialysis.app.ui.home.tabs

import androidx.lifecycle.viewModelScope
import com.dialysis.app.R
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.UrineLogRequest
import com.dialysis.app.data.network.response.UrineHistoryItem
import com.dialysis.app.sharepref.LocalUrineSample
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
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
    val urineSamplesModeState = collectStateUI(SettingsState::urineSamplesMode)
    val urineAmountInputState = collectStateUI(SettingsState::urineAmountInput)
    val urineNoteInputState = collectStateUI(SettingsState::urineNoteInput)
    val isSavingUrineSampleState = collectStateUI(SettingsState::isSavingUrineSample)
    val urineSaveSuccessState = collectStateUI(SettingsState::urineSaveSuccess)
    val urineSaveErrorState = collectStateUI(SettingsState::urineSaveError)
    val urineSaveErrorResIdState = collectStateUI(SettingsState::urineSaveErrorResId)
    val isLoadingUrineSamplesState = collectStateUI(SettingsState::isLoadingUrineSamples)
    val urineSamplesState = collectStateUI(SettingsState::urineSamples)
    val urineSamplesErrorState = collectStateUI(SettingsState::urineSamplesError)
    val urineSamplesErrorResIdState = collectStateUI(SettingsState::urineSamplesErrorResId)

    private val deleteAccountRequestInFlight = AtomicBoolean(false)
    private val urineSaveRequestInFlight = AtomicBoolean(false)

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

    fun openUrineSamples() {
        setState { copy(urineSamplesMode = UrineSamplesMode.Main) }
    }

    fun closeUrineSamples() {
        setState {
            copy(
                urineSamplesMode = UrineSamplesMode.Main,
                urineSaveSuccess = false,
                urineSaveError = null,
                urineSaveErrorResId = null,
                urineSamplesError = null,
                urineSamplesErrorResId = null
            )
        }
    }

    fun openAddTodayUrineSample() {
        setState {
            copy(
                urineSamplesMode = UrineSamplesMode.AddToday,
                urineAmountInput = "",
                urineNoteInput = "",
                urineSaveSuccess = false,
                urineSaveError = null,
                urineSaveErrorResId = null
            )
        }
    }

    fun openUrineSamplesHistory() {
        setState {
            copy(
                urineSamplesMode = UrineSamplesMode.History,
                urineSamplesError = null,
                urineSamplesErrorResId = null
            )
        }
        loadUrineSamples()
    }

    fun updateUrineAmount(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(5)
        setState {
            copy(
                urineAmountInput = sanitized,
                urineSaveSuccess = false,
                urineSaveError = null,
                urineSaveErrorResId = null
            )
        }
    }

    fun updateUrineNote(value: String) = setState {
        copy(
            urineNoteInput = value.take(250),
            urineSaveSuccess = false,
            urineSaveError = null,
            urineSaveErrorResId = null
        )
    }

    fun clearUrineSaveMessage() = setState {
        copy(urineSaveSuccess = false, urineSaveError = null, urineSaveErrorResId = null)
    }

    fun saveTodayUrineSample() {
        getState { state ->
            if (state.isSavingUrineSample) return@getState
            val amountMl = state.urineAmountInput.toIntOrNull()
            if (amountMl == null || amountMl < 0) {
                setState { copy(urineSaveErrorResId = R.string.settings_urine_amount_invalid) }
                return@getState
            }
            if (!urineSaveRequestInFlight.compareAndSet(false, true)) return@getState
            setState {
                copy(
                    isSavingUrineSample = true,
                    urineSaveSuccess = false,
                    urineSaveError = null,
                    urineSaveErrorResId = null
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val loggedAt = Instant.now().toString()
                    val note = state.urineNoteInput.trim().takeIf { it.isNotBlank() }
                    val clientId = UUID.randomUUID().toString()
                    val request = UrineLogRequest(
                        amount = amountMl,
                        loggedAt = loggedAt,
                        note = note,
                        clientId = clientId
                    )
                    if (accountSharePref.getToken().isNotBlank()) {
                        networkManager.resolve {
                            networkManager.appServices.logUrine(request)
                        }
                    }

                    userProfileSharePref.saveLocalUrineSample(
                        amountMl = amountMl,
                        loggedAt = loggedAt,
                        note = note,
                        clientId = clientId
                    )
                    val dailyWaterGoalMl = calculateDailyWaterGoalMl(amountMl)
                    userProfileSharePref.saveDailyUrineMl(amountMl)
                    userProfileSharePref.saveDailyWaterGoalMl(dailyWaterGoalMl)
                    setState {
                        copy(
                            isSavingUrineSample = false,
                            urineSaveSuccess = true,
                            urineAmountInput = "",
                            urineNoteInput = ""
                        )
                    }
                } finally {
                    urineSaveRequestInFlight.set(false)
                    getState { currentState ->
                        if (currentState.isSavingUrineSample) {
                            setState { copy(isSavingUrineSample = false) }
                        }
                    }
                }
            }
        }
    }

    fun loadUrineSamples() {
        val localSamples = userProfileSharePref.getLocalUrineSamples()
            .map { it.toUiModel() }
            .sortedByDescending { it.sampleTimeMillis ?: 0L }
        if (accountSharePref.getToken().isBlank()) {
            setState {
                copy(
                    isLoadingUrineSamples = false,
                    urineSamples = localSamples,
                    urineSamplesError = null,
                    urineSamplesErrorResId = null
                )
            }
            return
        }
        setState {
            copy(
                isLoadingUrineSamples = true,
                urineSamplesError = null,
                urineSamplesErrorResId = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = networkManager.resolve { networkManager.appServices.getUrineHistory() }
            if (result.isSuccess) {
                val remoteSamples = result.getOrNull().orEmpty()
                val remoteClientIds = remoteSamples.mapNotNull { it.clientId }.toSet()
                val mergedSamples = remoteSamples.map { it.toUiModel() } +
                    localSamples.filter { local ->
                        val localClientId = userProfileSharePref.getLocalUrineSamples()
                            .firstOrNull { it.id == local.id }
                            ?.clientId
                        localClientId == null || localClientId !in remoteClientIds
                    }
                setState {
                    copy(
                        isLoadingUrineSamples = false,
                        urineSamples = mergedSamples
                            .sortedByDescending { it.sampleTimeMillis ?: 0L }
                    )
                }
            } else {
                setState {
                    copy(
                        isLoadingUrineSamples = false,
                        urineSamples = localSamples,
                        urineSamplesError = null,
                        urineSamplesErrorResId = null
                    )
                }
            }
        }
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

    private fun calculateDailyWaterGoalMl(urineAmountMl: Int): Int {
        return LOCAL_BASE_DAILY_WATER_GOAL_ML + urineAmountMl
    }

    private fun UrineHistoryItem.toUiModel(): UrineSampleUiModel {
        return UrineSampleUiModel(
            id = id,
            amountMl = value,
            sampleTimeMillis = parseApiInstantMillis(fromDate),
            note = note?.takeIf { it.isNotBlank() }
        )
    }

    private fun LocalUrineSample.toUiModel(): UrineSampleUiModel {
        return UrineSampleUiModel(
            id = id,
            amountMl = amountMl,
            sampleTimeMillis = parseApiInstantMillis(loggedAt),
            note = note?.takeIf { it.isNotBlank() }
        )
    }

    private fun parseApiInstantMillis(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
    }

    private companion object {
        private const val LOCAL_BASE_DAILY_WATER_GOAL_ML = 500
    }
}
