package com.dialysis.app.ui.statistics

import androidx.compose.ui.graphics.Color

enum class StatisticsTab(val title: String) {
    MAIN("Chính"),
    BEVERAGES("Đồ uống"),
    BY_DAY("Theo ngày"),
    MONTHLY("Tháng")
}

enum class BeverageFilterPeriod(val title: String) {
    LAST_7_DAYS("7 ngày qua"),
    LAST_30_DAYS("30 ngày qua"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    THIS_YEAR("Năm nay"),
    LAST_YEAR("Năm trước")
}

data class StatisticsUiState(
    val todayTotalMl: Int,
    val dailyGoalMl: Int,
    val weeklyStats: WeeklyStatsUi,
    val beverageStats: List<BeverageStatUi>,
    val weeklyBeverageStats: List<BeverageStatUi>,
    val dailyStats: List<DailyStatUi>,
    val monthSummaries: List<MonthSummaryUi>,
    val currentMonthDays: List<MonthDayUi>
) {
    val todayPercentage: Int = percentOf(todayTotalMl, dailyGoalMl)
    val totalConsumptionMl: Int = beverageStats.sumOf { it.volumeMl }
}

data class WeeklyStatsUi(
    val dailyStats: List<DailyStatUi>,
    val weeklyTotalMl: Int,
    val averagePercentage: Double
)

data class DailyStatUi(
    val dateMillis: Long,
    val label: String,
    val listTitle: String,
    val totalMl: Int,
    val percentage: Int
)

data class BeverageStatUi(
    val visual: BeverageVisualUi,
    val volumeMl: Int,
    val percentage: Int
)

data class BeverageVisualUi(
    val key: String,
    val title: String,
    val icon: String,
    val color: Color
)

data class MonthSummaryUi(
    val monthStartMillis: Long,
    val title: String,
    val totalMl: Int,
    val averagePercentage: Int,
    val daysInMonth: Int
)

data class MonthDayUi(
    val dateMillis: Long,
    val day: Int,
    val percentage: Int,
    val totalMl: Int,
    val isToday: Boolean,
    val isInCurrentMonth: Boolean = true
)

internal fun percentOf(value: Int, goal: Int): Int {
    if (goal <= 0) return 0
    return ((value / goal.toFloat()) * 100).toInt().coerceIn(0, 100)
}
