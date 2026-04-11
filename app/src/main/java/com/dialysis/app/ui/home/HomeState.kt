package com.dialysis.app.ui.home

import com.dialysis.app.base.BaseState
import com.dialysis.app.config.AppGoals
import com.dialysis.app.data.local.model.DailyTotal

data class HomeDrinkItemState(
    val amount: String,
    val name: String,
    val time: String,
)

data class HomeState(
    val drinks: List<HomeDrinkItemState> = emptyList(),
    val showDrinkListSheet: Boolean = false,
    val showDailyReportSheet: Boolean = false,
    val selectedDrinkName: String? = null,
    val todayTotalMl: Int = 0,
    val weekTotalMl: Int = 0,
    val monthTotalMl: Int = 0,
    val weekDailyMl: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val dailyWaterGoalMl: Int = AppGoals.DAILY_WATER_GOAL_ML,
    val isLoggedIn: Boolean = false,
    val showSymptomSheet: Boolean = false,
    val symptoms: List<String> = emptyList(),
    val selectedSymptom: String? = null,
    val symptomNotes: String = "",
    val isSymptomsLoading: Boolean = false,
    val isSubmittingSymptom: Boolean = false,
    val showSymptomSubmitSuccessToast: Boolean = false,
    val isHistorySyncing: Boolean = false,
) : BaseState
