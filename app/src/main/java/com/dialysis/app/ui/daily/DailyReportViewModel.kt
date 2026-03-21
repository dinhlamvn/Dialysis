package com.dialysis.app.ui.daily

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyReportViewModel(
    private val waterTrackingRepository: WaterTrackingRepository
) : BaseViewModel<DailyReportState>(DailyReportState()) {

    val drinksState = flowOf(DailyReportState::drinks).collectStateUI(emptyList())
    val progressState = flowOf(DailyReportState::progress).collectStateUI(0f)
    val progressTextState = flowOf(DailyReportState::progressText).collectStateUI("0%")
    val showDrinkListSheetState = flowOf(DailyReportState::showDrinkListSheet).collectStateUI(false)
    val selectedDrinkNameState = flowOf(DailyReportState::selectedDrinkName).collectStateUI(null)
    val showDeleteDialogState = flowOf(DailyReportState::showDeleteDialog).collectStateUI(false)
    val deletingDrinkTitleState = flowOf(DailyReportState::deletingDrinkTitle).collectStateUI("")

    init {
        val goalMl = 1200f
        combine(
            waterTrackingRepository.observeTodayEntries(),
            waterTrackingRepository.observeTodayTotalMl()
        ) { entries, todayTotal ->
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val mapped = entries.map { entry ->
                DailyDrinkItemState(
                    id = entry.id,
                    title = "${entry.drinkName} - ${entry.amountMl} ml",
                    subtitle = "${entry.amountMl} ml của ${entry.drinkName.lowercase()} còn lại",
                    time = formatter.format(Date(entry.createdAt))
                )
            }
            val progress = (todayTotal / goalMl).coerceIn(0f, 1f)
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

    fun addDrink(name: String, amount: String) {
        setState { copy(selectedDrinkName = null) }
        viewModelScope.launch(Dispatchers.IO) {
            waterTrackingRepository.addEntry(
                drinkName = name,
                amountMl = amount.extractMlValue()
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
                waterTrackingRepository.deleteEntry(entryId)
            }
        }
    }
}

private fun String.extractMlValue(): Int {
    val digits = replace("[^0-9]".toRegex(), "")
    return digits.toIntOrNull() ?: 0
}
