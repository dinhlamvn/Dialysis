package com.dialysis.app.ui.home

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.SymptomLogRequest
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.sync.WaterIntakeRecorder
import com.dialysis.app.sync.WaterIntakeSyncScheduler
import com.dialysis.app.sync.WaterSyncEngine
import com.dialysis.app.sync.WaterSyncOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val waterTrackingRepository: WaterTrackingRepository,
    private val userProfileSharePref: UserProfileSharePref,
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager,
    private val waterIntakeSyncScheduler: WaterIntakeSyncScheduler,
    private val waterSyncEngine: WaterSyncEngine,
    private val waterIntakeRecorder: WaterIntakeRecorder
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
    val allWaterEntriesState = collectStateUI(HomeState::allWaterEntries)
    val dailyWaterGoalMlState = collectStateUI(HomeState::dailyWaterGoalMl)
    val isLoggedInState = collectStateUI(HomeState::isLoggedIn)
    val showSymptomSheetState = collectStateUI(HomeState::showSymptomSheet)
    val symptomsState = collectStateUI(HomeState::symptoms)
    val selectedSymptomState = collectStateUI(HomeState::selectedSymptom)
    val symptomNotesState = collectStateUI(HomeState::symptomNotes)
    val showSymptomNotesErrorState = collectStateUI(HomeState::showSymptomNotesError)
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

        waterTrackingRepository.observeAllEntries()
            .onEach { entries -> setState { copy(allWaterEntries = entries) } }
            .launchIn(viewModelScope)

        userProfileSharePref.observeDailyWaterGoalMl()
            .onEach { goalMl -> setState { copy(dailyWaterGoalMl = goalMl) } }
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
            waterIntakeRecorder.record(
                drinkName = name,
                amountMl = amount.extractMlValue(),
                loggedAt = System.currentTimeMillis()
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
            showSymptomNotesError = false,
            isSubmittingSymptom = false
        )
    }

    fun selectSymptom(symptom: String) = setState {
        copy(
            selectedSymptom = symptom,
            showSymptomNotesError = symptom.isOtherSymptom() && symptomNotes.isBlank()
        )
    }

    fun updateSymptomNotes(notes: String) = setState {
        copy(
            symptomNotes = notes,
            showSymptomNotesError = false
        )
    }

    fun submitSymptomLog() {
        getState { state ->
            val symptom = state.selectedSymptom?.trim().orEmpty()
            val notes = state.symptomNotes.trim()
            if (symptom.isBlank() || state.isSubmittingSymptom) return@getState
            if (symptom.isOtherSymptom() && notes.isBlank()) {
                setState { copy(showSymptomNotesError = true) }
                return@getState
            }

            setState {
                copy(
                    isSubmittingSymptom = true,
                    showSymptomNotesError = false
                )
            }
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
                            showSymptomNotesError = false,
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

    fun resetAfterSignOut() {
        setState {
            copy(
                dailyWaterGoalMl = userProfileSharePref.getDailyWaterGoalMl(),
                isLoggedIn = false,
                showSymptomSheet = false,
                symptoms = emptyList(),
                selectedSymptom = null,
                symptomNotes = "",
                showSymptomNotesError = false,
                isSymptomsLoading = false,
                isSubmittingSymptom = false,
                isHistorySyncing = false
            )
        }
    }

    private fun syncWaterHistoryIfNeeded() {
        getState { state ->
            if (!state.isLoggedIn || state.isHistorySyncing) return@getState
            setState { copy(isHistorySyncing = true) }
            viewModelScope.launch(Dispatchers.IO) {
                val outcome = try {
                    waterSyncEngine.runSyncSequence()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    WaterSyncOutcome.FAILED
                }
                setState { copy(isHistorySyncing = false) }
                if (outcome == WaterSyncOutcome.FAILED) {
                    enqueueWaterSyncIfLoggedIn()
                }
            }
        }
    }

    private fun enqueueWaterSyncIfLoggedIn() {
        if (accountSharePref.getToken().isNotBlank()) {
            waterIntakeSyncScheduler.enqueue()
        }
    }
}

private fun String.isOtherSymptom(): Boolean {
    val normalized = trim().lowercase(Locale.ROOT)
    return normalized.contains("other") ||
        normalized.contains("khác") ||
        normalized.contains("khac")
}

private fun String.extractMlValue(): Int {
    val digits = replace("[^0-9]".toRegex(), "")
    return digits.toIntOrNull() ?: 0
}

