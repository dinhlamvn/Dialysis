package com.dialysis.app.ui.weight

import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.data.network.response.WeightChartItem
import com.dialysis.app.data.network.response.WeightChartStats
import java.util.Calendar
import kotlin.math.max

internal data class WeightChartResult(
    val points: List<WeightChartPoint>,
    val xAxisLabels: List<WeightAxisLabel>,
    val yMin: Float,
    val yMax: Float,
)

internal fun buildWeightChart(
    tab: WeightReportTab,
    entries: List<WeightEntryEntity>,
    range: WeightReportRange,
    goalWeight: Float
): WeightChartResult {
    val points = when (tab) {
        WeightReportTab.MONTH -> buildMonthPoints(entries, range.monthDays)
        WeightReportTab.YEAR -> buildYearPoints(entries)
    }
    return buildWeightChartFromPoints(points, buildWeightAxisLabels(tab, range), goalWeight, stats = null)
}

internal fun buildWeightChartFromPoints(
    points: List<WeightChartPoint>,
    labels: List<WeightAxisLabel>,
    goalWeight: Float,
    stats: WeightChartStats?
): WeightChartResult {
    val values = points.map { it.value } + listOf(goalWeight) + listOfNotNull(
        stats?.min?.toFloat(),
        stats?.max?.toFloat()
    )
    val minValue = values.min()
    val maxValue = values.max()
    val span = maxValue - minValue
    val margin = max(0.5f, span * 0.2f)
    return WeightChartResult(
        points = points,
        xAxisLabels = labels,
        yMin = max(0f, minValue - margin),
        yMax = max(minValue + margin, maxValue + margin)
    )
}

internal fun buildWeightAxisLabels(tab: WeightReportTab, range: WeightReportRange): List<WeightAxisLabel> {
    return when (tab) {
        WeightReportTab.MONTH -> buildMonthLabels(range.monthDays)
        WeightReportTab.YEAR -> buildYearLabels()
    }
}

internal fun List<WeightChartItem>.toChartPoints(
    tab: WeightReportTab,
    range: WeightReportRange
): List<WeightChartPoint> {
    return when (tab) {
        WeightReportTab.MONTH -> toMonthChartPoints(range)
        WeightReportTab.YEAR -> toYearChartPoints(range)
    }
}

private fun buildMonthPoints(entries: List<WeightEntryEntity>, monthDays: Int): List<WeightChartPoint> {
    if (monthDays <= 0 || entries.isEmpty()) return emptyList()
    val today = startOfWeightDay(System.currentTimeMillis())
    val latestByDay = entries
        .filter { it.dayStartMillis <= today }
        .groupBy { weightDayOfMonth(it.dayStartMillis) }
        .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt }?.weightKg ?: 0f }
    return latestByDay.entries.sortedBy { it.key }.map { (day, value) ->
        WeightChartPoint(xRatio = dayToRatio(day, monthDays), value = value)
    }
}

private fun buildYearPoints(entries: List<WeightEntryEntity>): List<WeightChartPoint> {
    if (entries.isEmpty()) return emptyList()
    val today = startOfWeightDay(System.currentTimeMillis())
    val averageByMonth = entries
        .filter { it.dayStartMillis <= today }
        .groupBy { weightMonthOfYear(it.dayStartMillis) }
        .mapValues { (_, list) -> list.map { it.weightKg }.average().toFloat() }
    return averageByMonth.entries.sortedBy { it.key }.map { (month, value) ->
        WeightChartPoint(xRatio = monthToRatio(month), value = value)
    }
}

private fun buildMonthLabels(days: Int): List<WeightAxisLabel> {
    if (days <= 0) return emptyList()
    return (5..days step 5).map { day ->
        WeightAxisLabel(xRatio = dayToRatio(day, days), label = day.toString())
    }
}

private fun buildYearLabels(): List<WeightAxisLabel> {
    return (2..12 step 2).map { month ->
        WeightAxisLabel(xRatio = monthToRatio(month), label = "Th$month")
    }
}

private fun List<WeightChartItem>.toMonthChartPoints(range: WeightReportRange): List<WeightChartPoint> {
    return mapNotNull { item ->
        val millis = item.date?.let(::parseWeightApiDate) ?: return@mapNotNull null
        if (millis > startOfWeightDay(System.currentTimeMillis())) return@mapNotNull null
        val value = item.resolvedValue?.toFloat() ?: return@mapNotNull null
        WeightChartPoint(xRatio = dayToRatio(weightDayOfMonth(millis), range.monthDays), value = value)
    }.sortedBy { it.xRatio }
}

private fun List<WeightChartItem>.toYearChartPoints(range: WeightReportRange): List<WeightChartPoint> {
    return mapNotNull { item ->
        val month = item.month
            ?: item.date?.let(::parseWeightApiMonth)
            ?: item.label?.filter { it.isDigit() }?.toIntOrNull()
            ?: return@mapNotNull null
        val value = item.resolvedValue?.toFloat() ?: return@mapNotNull null
        if (monthStartMillis(range.year, month) > startOfWeightDay(System.currentTimeMillis())) return@mapNotNull null
        WeightChartPoint(xRatio = monthToRatio(month), value = value)
    }.sortedBy { it.xRatio }
}

private fun dayToRatio(day: Int, monthDays: Int): Float {
    return ((day - 1) / max(1, monthDays - 1).toFloat()).coerceIn(0f, 1f)
}

private fun monthToRatio(month: Int): Float {
    return ((month - 1) / 11f).coerceIn(0f, 1f)
}

private fun monthStartMillis(year: Int, month: Int): Long {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
