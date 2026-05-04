package com.dialysis.app.ui.statistics

import com.dialysis.app.data.local.entity.WaterEntryEntity
import java.util.Calendar
import kotlin.math.roundToInt

object StatisticsCalculator {
    fun build(
        entries: List<WaterEntryEntity>,
        dailyGoalMl: Int,
        beverageFilterPeriod: BeverageFilterPeriod
    ): StatisticsUiState {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val todayTotal = entries.totalInDay(todayStart)
        val dailyStats = buildLastSevenDailyStats(entries, dailyGoalMl, now)
        val weeklyStats = WeeklyStatsUi(
            dailyStats = dailyStats,
            weeklyTotalMl = dailyStats.sumOf { it.totalMl },
            averagePercentage = dailyStats.map { it.percentage }.average().takeIf { !it.isNaN() } ?: 0.0
        )
        return StatisticsUiState(
            todayTotalMl = todayTotal,
            dailyGoalMl = dailyGoalMl,
            weeklyStats = weeklyStats,
            weeklyBeverageStats = beverageStats(entries.inLastDays(7, now)),
            beverageStats = beverageStats(entries.inPeriod(beverageFilterPeriod, now)),
            dailyStats = entries.toAllDailyStats(dailyGoalMl),
            monthSummaries = buildMonthSummaries(entries, dailyGoalMl, now),
            currentMonthDays = buildMonthDays(entries, dailyGoalMl, startOfMonth(now))
        )
    }

    fun monthDays(entries: List<WaterEntryEntity>, dailyGoalMl: Int, monthStartMillis: Long): List<MonthDayUi> {
        return buildMonthDays(entries, dailyGoalMl, monthStartMillis)
    }

    private fun buildLastSevenDailyStats(entries: List<WaterEntryEntity>, goal: Int, now: Long): List<DailyStatUi> {
        return (6 downTo 0).map { offset ->
            val day = Calendar.getInstance().apply { timeInMillis = now; add(Calendar.DAY_OF_MONTH, -offset) }
            dayStat(entries, goal, startOfDay(day.timeInMillis))
        }
    }

    private fun List<WaterEntryEntity>.toAllDailyStats(goal: Int): List<DailyStatUi> {
        val grouped = groupBy { startOfDay(it.createdAt) }.toSortedMap(compareByDescending { it })
        return grouped.map { (dayStart, dayEntries) -> dayStat(dayEntries, goal, dayStart) }
    }

    private fun dayStat(entries: List<WaterEntryEntity>, goal: Int, dayStart: Long): DailyStatUi {
        val total = entries.filter { startOfDay(it.createdAt) == dayStart }.sumOf { it.amountMl }
        return DailyStatUi(
            dateMillis = dayStart,
            label = dayLabel(dayStart),
            listTitle = listDayTitle(dayStart),
            totalMl = total,
            percentage = percentOf(total, goal)
        )
    }

    private fun beverageStats(entries: List<WaterEntryEntity>): List<BeverageStatUi> {
        val grouped = entries.groupBy { DrinkStatisticsCatalog.resolve(it.drinkName) }
        val total = entries.sumOf { it.amountMl }
        return DrinkStatisticsCatalog.visuals.map { visual ->
            val volume = grouped[visual].orEmpty().sumOf { it.amountMl }
            BeverageStatUi(visual = visual, volumeMl = volume, percentage = percentOf(volume, total))
        }.sortedByDescending { it.volumeMl }
    }

    private fun buildMonthSummaries(entries: List<WaterEntryEntity>, goal: Int, now: Long): List<MonthSummaryUi> {
        val currentMonth = startOfMonth(now)
        return (0 until 12).map { offset ->
            val monthStart = Calendar.getInstance().apply { timeInMillis = currentMonth; add(Calendar.MONTH, -offset) }.timeInMillis
            val days = buildMonthDays(entries, goal, monthStart)
            MonthSummaryUi(
                monthStartMillis = monthStart,
                title = monthYearTitle(monthStart),
                totalMl = days.sumOf { it.totalMl },
                averagePercentage = days.map { it.percentage }.average().roundToInt().coerceAtLeast(0),
                daysInMonth = days.size
            )
        }
    }

    private fun buildMonthDays(entries: List<WaterEntryEntity>, goal: Int, monthStartMillis: Long): List<MonthDayUi> {
        val calendar = Calendar.getInstance().apply { timeInMillis = monthStartMillis }
        val today = startOfDay(System.currentTimeMillis())
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            val date = Calendar.getInstance().apply { timeInMillis = monthStartMillis; set(Calendar.DAY_OF_MONTH, day) }
            val dayStart = startOfDay(date.timeInMillis)
            val total = entries.totalInDay(dayStart)
            MonthDayUi(dayStart, day, percentOf(total, goal), total, dayStart == today)
        }
    }

    private fun List<WaterEntryEntity>.inLastDays(days: Int, now: Long): List<WaterEntryEntity> {
        val start = Calendar.getInstance().apply { timeInMillis = startOfDay(now); add(Calendar.DAY_OF_MONTH, -(days - 1)) }.timeInMillis
        return filter { it.createdAt >= start && it.createdAt <= now }
    }

    private fun List<WaterEntryEntity>.inPeriod(period: BeverageFilterPeriod, now: Long): List<WaterEntryEntity> {
        val start = when (period) {
            BeverageFilterPeriod.LAST_7_DAYS -> Calendar.getInstance().apply { timeInMillis = startOfDay(now); add(Calendar.DAY_OF_MONTH, -6) }.timeInMillis
            BeverageFilterPeriod.LAST_30_DAYS -> Calendar.getInstance().apply { timeInMillis = startOfDay(now); add(Calendar.DAY_OF_MONTH, -29) }.timeInMillis
            BeverageFilterPeriod.THIS_MONTH -> startOfMonth(now)
            BeverageFilterPeriod.LAST_MONTH -> Calendar.getInstance().apply { timeInMillis = startOfMonth(now); add(Calendar.MONTH, -1) }.timeInMillis
            BeverageFilterPeriod.THIS_YEAR -> startOfYear(now)
            BeverageFilterPeriod.LAST_YEAR -> Calendar.getInstance().apply { timeInMillis = startOfYear(now); add(Calendar.YEAR, -1) }.timeInMillis
        }
        val end = when (period) {
            BeverageFilterPeriod.LAST_MONTH -> startOfMonth(now)
            BeverageFilterPeriod.LAST_YEAR -> startOfYear(now)
            else -> now + 1
        }
        return filter { it.createdAt >= start && it.createdAt < end }
    }

    private fun List<WaterEntryEntity>.totalInDay(dayStart: Long): Int = filter { startOfDay(it.createdAt) == dayStart }.sumOf { it.amountMl }
}

internal fun startOfDay(timeMillis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timeMillis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

internal fun startOfMonth(timeMillis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timeMillis; set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun startOfYear(timeMillis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timeMillis; set(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis
