package com.dialysis.app.ui.weight

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal data class WeightReportRange(
    val startMillis: Long,
    val endMillis: Long,
    val title: String,
    val year: Int,
    val monthNumber: Int? = null,
    val monthDays: Int = 0,
)

internal fun buildWeightReportRange(tab: WeightReportTab, offset: Int): WeightReportRange {
    val calendar = Calendar.getInstance()
    return when (tab) {
        WeightReportTab.MONTH -> calendar.buildMonthRange(offset)
        WeightReportTab.YEAR -> calendar.buildYearRange(offset)
    }
}

private fun Calendar.buildMonthRange(offset: Int): WeightReportRange {
    moveToStartOfMonth()
    add(Calendar.MONTH, offset)
    val start = timeInMillis
    val days = getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthNumber = get(Calendar.MONTH) + 1
    val year = get(Calendar.YEAR)
    add(Calendar.MONTH, 1)
    add(Calendar.MILLISECOND, -1)
    return WeightReportRange(
        startMillis = start,
        endMillis = timeInMillis,
        title = SimpleDateFormat("MMMM", Locale.getDefault()).format(time),
        year = year,
        monthNumber = monthNumber,
        monthDays = days
    )
}

private fun Calendar.buildYearRange(offset: Int): WeightReportRange {
    set(Calendar.MONTH, Calendar.JANUARY)
    moveToStartOfMonth()
    add(Calendar.YEAR, offset)
    val start = timeInMillis
    val year = get(Calendar.YEAR)
    add(Calendar.YEAR, 1)
    add(Calendar.MILLISECOND, -1)
    return WeightReportRange(startMillis = start, endMillis = timeInMillis, title = year.toString(), year = year)
}

private fun Calendar.moveToStartOfMonth() {
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
