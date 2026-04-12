package com.dialysis.app.ui.home

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.SymptomLogRequest
import com.dialysis.app.data.network.response.WaterIntakeResponse
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant

class HomeViewModel(
    private val waterTrackingRepository: WaterTrackingRepository,
    private val userProfileSharePref: UserProfileSharePref,
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<HomeState>(HomeState()) {

    val drinksState = collectStateUI(HomeState::drinks)
    val showDrinkListSheetState = collectStateUI(HomeState::showDrinkListSheet)
    val showDailyReportSheetState = collectStateUI(HomeState::showDailyReportSheet)
    val selectedDrinkNameState = collectStateUI(HomeState::selectedDrinkName)
    val todayTotalMlState = collectStateUI(HomeState::todayTotalMl)
    val weekTotalMlState = collectStateUI(HomeState::weekTotalMl)
    val monthTotalMlState = collectStateUI(HomeState::monthTotalMl)
    val weekDailyMlState = collectStateUI(HomeState::weekDailyMl)
    val dailyTotalsState = collectStateUI(HomeState::dailyTotals)
    val dailyWaterGoalMlState = collectStateUI(HomeState::dailyWaterGoalMl)
    val isLoggedInState = collectStateUI(HomeState::isLoggedIn)
    val showSymptomSheetState = collectStateUI(HomeState::showSymptomSheet)
    val symptomsState = collectStateUI(HomeState::symptoms)
    val selectedSymptomState = collectStateUI(HomeState::selectedSymptom)
    val symptomNotesState = collectStateUI(HomeState::symptomNotes)
    val isSymptomsLoadingState = collectStateUI(HomeState::isSymptomsLoading)
    val isSubmittingSymptomState = collectStateUI(HomeState::isSubmittingSymptom)
    val showSymptomSubmitSuccessToastState = collectStateUI(HomeState::showSymptomSubmitSuccessToast)
    val isHistorySyncingState = collectStateUI(HomeState::isHistorySyncing)

    init {
        setState {
            copy(
                dailyWaterGoalMl = userProfileSharePref.getDailyWaterGoalMl(),
                isLoggedIn = accountSharePref.getToken().isNotBlank()
            )
        }
        syncWaterHistoryIfNeeded()

        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        waterTrackingRepository.observeTodayEntries()
            .onEach { entries ->
                setState {
                    copy(
                        drinks = entries.map { entry ->
                            HomeDrinkItemState(
                                amount = "${entry.amountMl} ml",
                                name = entry.drinkName,
                                time = timeFormatter.format(Date(entry.createdAt))
                            )
                        }
                    )
                }
            }
            .launchIn(viewModelScope)

        waterTrackingRepository.observeTodayTotalMl()
            .onEach { total -> setState { copy(todayTotalMl = total) } }
            .launchIn(viewModelScope)

        waterTrackingRepository.observeWeekTotalMl()
            .onEach { total -> setState { copy(weekTotalMl = total) } }
            .launchIn(viewModelScope)

        waterTrackingRepository.observeMonthTotalMl()
            .onEach { total -> setState { copy(monthTotalMl = total) } }
            .launchIn(viewModelScope)

        waterTrackingRepository.observeWeekDailyMl()
            .onEach { totals -> setState { copy(weekDailyMl = totals) } }
            .launchIn(viewModelScope)

        waterTrackingRepository.observeAllDailyTotals()
            .onEach { totals -> setState { copy(dailyTotals = totals) } }
            .launchIn(viewModelScope)
    }

    fun openDrinkListSheet() = setState {
        copy(showDrinkListSheet = true)
    }

    fun closeDrinkListSheet() = setState {
        copy(showDrinkListSheet = false)
    }

    fun openDailyReportSheet() = setState {
        copy(showDailyReportSheet = true)
    }

    fun closeDailyReportSheet() = setState {
        copy(showDailyReportSheet = false)
    }

    fun onDrinkSelected(drinkName: String) = setState {
        copy(
            showDrinkListSheet = false,
            selectedDrinkName = drinkName
        )
    }

    fun dismissCreateDrinkSheet() = setState {
        copy(selectedDrinkName = null)
    }

    fun backToDrinkListFromCreate() = setState {
        copy(
            selectedDrinkName = null,
            showDrinkListSheet = true
        )
    }

    fun addDrink(name: String, amount: String, _time: String) {
        setState { copy(selectedDrinkName = null) }
        viewModelScope.launch(Dispatchers.IO) {
            waterTrackingRepository.addEntry(
                drinkName = name,
                amountMl = amount.extractMlValue()
            )
        }
    }

    fun openSymptomSheet() {
        getState { state ->
            if (!state.isLoggedIn) return@getState
            setState { copy(showSymptomSheet = true) }
            if (state.symptoms.isEmpty()) {
                loadSymptoms()
            }
        }
    }

    fun closeSymptomSheet() = setState {
        copy(
            showSymptomSheet = false,
            selectedSymptom = null,
            symptomNotes = "",
            isSubmittingSymptom = false
        )
    }

    fun selectSymptom(symptom: String) = setState { copy(selectedSymptom = symptom) }

    fun updateSymptomNotes(notes: String) = setState { copy(symptomNotes = notes) }

    fun submitSymptomLog() {
        getState { state ->
            val symptom = state.selectedSymptom?.trim().orEmpty()
            val notes = state.symptomNotes.trim()
            if (symptom.isBlank() || notes.isBlank() || state.isSubmittingSymptom) return@getState

            setState { copy(isSubmittingSymptom = true) }
            viewModelScope.launch(Dispatchers.IO) {
                val loggedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    .format(Date(System.currentTimeMillis()))
                val result = networkManager.resolveNullable {
                    networkManager.appServices.logSymptom(
                        SymptomLogRequest(
                            symptom = symptom,
                            notes = notes,
                            loggedAt = loggedAt
                        )
                    )
                }
                if (result.isSuccess) {
                    setState {
                        copy(
                            isSubmittingSymptom = false,
                            showSymptomSheet = false,
                            selectedSymptom = null,
                            symptomNotes = "",
                            showSymptomSubmitSuccessToast = true
                        )
                    }
                } else {
                    setState { copy(isSubmittingSymptom = false) }
                }
            }
        }
    }

    private fun loadSymptoms() {
        getState { state ->
            if (state.isSymptomsLoading) return@getState
            setState { copy(isSymptomsLoading = true) }
            viewModelScope.launch(Dispatchers.IO) {
                val result = networkManager.resolve {
                    networkManager.appServices.getSymptoms()
                }
                if (result.isSuccess) {
                    setState {
                        copy(
                            isSymptomsLoading = false,
                            symptoms = result.getOrNull().orEmpty()
                        )
                    }
                } else {
                    setState { copy(isSymptomsLoading = false) }
                }
            }
        }
    }

    fun clearSymptomSubmitSuccessToast() = setState {
        copy(showSymptomSubmitSuccessToast = false)
    }

    private fun syncWaterHistoryIfNeeded() {
        getState { state ->
            if (!state.isLoggedIn || state.isHistorySyncing) return@getState
            setState { copy(isHistorySyncing = true) }
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    syncWaterHistory()
                }
                setState { copy(isHistorySyncing = false) }
            }
        }
    }

    private suspend fun syncWaterHistory() {
        val history = fetchAllHistoryPages() ?: return
        val uniqueBySyncedId = history.distinctBy { it.id }
        val syncedIds = uniqueBySyncedId.map { it.id }
        val existingSyncedIds = waterTrackingRepository.getExistingSyncedIds(syncedIds)
        val missingEntries = uniqueBySyncedId
            .filterNot { it.id in existingSyncedIds }
            .map { it.toEntity() }
        waterTrackingRepository.insertSyncedEntries(missingEntries)

        val fetchedSyncedIdSet = syncedIds.toSet()
        val localSyncedEntries = waterTrackingRepository.getSyncedEntries()
        val staleLocalEntryIds = localSyncedEntries
            .filter { localEntry ->
                val syncedId = localEntry.syncedId
                syncedId != null && syncedId !in fetchedSyncedIdSet
            }
            .map { it.id }
        waterTrackingRepository.deleteEntriesLocalOnly(staleLocalEntryIds)
    }

    private suspend fun fetchAllHistoryPages(): List<WaterIntakeResponse>? {
        val allItems = mutableListOf<WaterIntakeResponse>()
        var page = 1
        while (page <= MAX_HISTORY_SYNC_PAGE) {
            val result = networkManager.resolve {
                networkManager.appServices.getWaterHistory(page = page)
            }
            if (result.isFailure) return null
            val pageItems = result.getOrNull().orEmpty()
            if (pageItems.isEmpty()) break
            allItems += pageItems
            page++
        }
        return allItems
    }

    private fun WaterIntakeResponse.toEntity(): WaterEntryEntity {
        val createdAtMillis = runCatching { Instant.parse(loggedAt).toEpochMilli() }
            .getOrElse { System.currentTimeMillis() }
        return WaterEntryEntity(
            drinkName = drinkName,
            amountMl = rawAmount,
            createdAt = createdAtMillis,
            syncedId = id
        )
    }
}

private fun String.extractMlValue(): Int {
    val digits = replace("[^0-9]".toRegex(), "")
    return digits.toIntOrNull() ?: 0
}

private const val MAX_HISTORY_SYNC_PAGE = 500
