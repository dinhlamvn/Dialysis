package com.dialysis.app.ui.info

import com.dialysis.app.base.BaseState

data class InfoState(
    val currentStep: Int = 0,
    val gender: Int = 1,
    val weight: Int = 50,
    val height: Int = 170,
    val age: Int = 30,
    val name: String = "",
    val phone: String = "",
    val dialysisStartYear: Int = 0,
    val dialysisFreqWeek: Int = 0,
    val dailyUrineMl: Int = 0,
    val isCalculatingGoal: Boolean = false,
    val calculateGoalStatus: CalculateGoalStatus = CalculateGoalStatus.None,
): BaseState

sealed interface CalculateGoalStatus {
    data object None : CalculateGoalStatus
    data object Success : CalculateGoalStatus
    data class Failed(val message: String) : CalculateGoalStatus
}
