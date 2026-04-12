package com.dialysis.app.ui.daily

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DailyReportViewModel(
    private val waterTrackingRepository: WaterTrackingRepository,
    private val userProfileSharePref: UserProfileSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<DailyReportState>(DailyReportState()) {

    private val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())

    val selectedDateLabelState = collectStateUI(DailyReportState::selectedDateLabel)
    val drinksState = collectStateUI(DailyReportState::drinks)
    val progressState = collectStateUI(DailyReportState::progress)
    val progressTextState = collectStateUI(DailyReportState::progressText)
    val showDrinkListSheetState = collectStateUI(DailyReportState::showDrinkListSheet)
    val selectedDrinkNameState = collectStateUI(DailyReportState::selectedDrinkName)
    val showDeleteDialogState = collectStateUI(DailyReportState::showDeleteDialog)
    val deletingDrinkTitleState = collectStateUI(DailyReportState::deletingDrinkTitle)

    init {
        val goalMl = userProfileSharePref.getDailyWaterGoalMl().toFloat()

        selectedDateMillis
            .onEach { dateMillis ->
                setState {
                    copy(selectedDateLabel = formatSelectedDate(dateMillis))
                }
            }
            .launchIn(viewModelScope)

        combine(
            selectedDateMillis.flatMapLatest(waterTrackingRepository::observeEntriesForDate),
            selectedDateMillis.flatMapLatest(waterTrackingRepository::observeTotalMlForDate)
        ) { entries, totalForDate ->
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val mapped = entries.map { entry ->
                DailyDrinkItemState(
                    id = entry.id,
                    title = "${entry.drinkName} - ${entry.amountMl} ml",
                    subtitle = "${entry.amountMl} ml của ${entry.drinkName.lowercase()} còn lại",
                    time = formatter.format(Date(entry.createdAt))
                )
            }
            val progress = (totalForDate / goalMl).coerceIn(0f, 1f)
            val percent = (progress * 100).toInt()
            Triple(mapped, progress, "$percent%")
        }.onEach { (drinks, progress, progressText) ->
            setState {
                copy(
                    drinks = drinks,
                    progress = progress,
                    progressText = progressText
                )
            }
        }.launchIn(viewModelScope)
    }

    fun showDateReport(dateMillis: Long) {
        selectedDateMillis.value = dateMillis
    }

    fun showTodayReport() {
        selectedDateMillis.value = System.currentTimeMillis()
    }

    fun openDrinkListSheet() = setState {
        copy(showDrinkListSheet = true)
    }

    fun closeDrinkListSheet() = setState {
        copy(showDrinkListSheet = false)
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

    fun addDrink(name: String, amount: String) {
        setState { copy(selectedDrinkName = null) }
        viewModelScope.launch(Dispatchers.IO) {
            waterTrackingRepository.addEntry(
                drinkName = name,
                amountMl = amount.extractMlValue()
            )
        }
    }

    fun addDrink(name: String, amount: String, timeText: String) {
        setState { copy(selectedDrinkName = null) }
        viewModelScope.launch(Dispatchers.IO) {
            waterTrackingRepository.addEntry(
                drinkName = name,
                amountMl = amount.extractMlValue(),
                createdAt = mergeDateAndTime(
                    dateMillis = selectedDateMillis.value,
                    timeText = timeText
                )
            )
        }
    }

    fun requestDelete(drink: DailyDrinkItemState) = setState {
        copy(
            showDeleteDialog = true,
            deletingDrinkId = drink.id,
            deletingDrinkTitle = drink.title
        )
    }

    fun dismissDeleteDialog() = setState {
        copy(
            showDeleteDialog = false,
            deletingDrinkId = null,
            deletingDrinkTitle = ""
        )
    }

    fun confirmDeleteDrink() {
        getState { state ->
            val entryId = state.deletingDrinkId ?: return@getState
            dismissDeleteDialog()
            viewModelScope.launch(Dispatchers.IO) {
                val entry = waterTrackingRepository.getEntryById(entryId)
                val syncedId = entry?.syncedId
                if (syncedId != null) {
                    val remoteDeleteResult = networkManager.resolveNullable {
                        networkManager.appServices.deleteWaterIntake(syncedId)
                    }
                    if (remoteDeleteResult.isSuccess) {
                        waterTrackingRepository.deleteEntryLocalOnly(entryId)
                    } else {
                        waterTrackingRepository.deleteEntry(entryId)
                    }
                } else {
                    waterTrackingRepository.deleteEntry(entryId)
                }
            }
        }
    }
}

private fun formatSelectedDate(dateMillis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return "${calendar.get(Calendar.DAY_OF_MONTH)} tháng ${calendar.get(Calendar.MONTH) + 1}"
}

private fun mergeDateAndTime(dateMillis: Long, timeText: String): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val parts = timeText.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun String.extractMlValue(): Int {
    val digits = replace("[^0-9]".toRegex(), "")
    return digits.toIntOrNull() ?: 0
}
