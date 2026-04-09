package com.dialysis.app.ui.home

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WaterTrackingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val waterTrackingRepository: WaterTrackingRepository
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

    init {
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
}

private fun String.extractMlValue(): Int {
    val digits = replace("[^0-9]".toRegex(), "")
    return digits.toIntOrNull() ?: 0
}
