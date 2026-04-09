package com.dialysis.app.ui.weight

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max

@OptIn(ExperimentalCoroutinesApi::class)
class WeightViewModel(
    private val weightTrackingRepository: WeightTrackingRepository,
    private val userProfileSharePref: UserProfileSharePref
) : BaseViewModel<WeightState>(WeightState()) {

    private val selectedTabFlow = MutableStateFlow(WeightReportTab.MONTH)
    private val periodOffsetFlow = MutableStateFlow(0)

    val initialWeightKgState = collectStateUI(WeightState::initialWeightKg)
    val currentWeightKgState = collectStateUI(WeightState::currentWeightKg)
    val selectedTabState = collectStateUI(WeightState::selectedTab)
    val periodTitleState = collectStateUI(WeightState::periodTitle)
    val showAddWeightSheetState = collectStateUI(WeightState::showAddWeightSheet)
    val draftWeightKgState = collectStateUI(WeightState::draftWeightKg)
    val chartDataState = collectStateUI(WeightState::chartData)
    val xAxisLabelsState = collectStateUI(WeightState::xAxisLabels)
    val yMinState = collectStateUI(WeightState::yMin)
    val yMaxState = collectStateUI(WeightState::yMax)

    init {
        val initialWeight = userProfileSharePref.getInitialWeightKg().toFloat()
        setState { copy(initialWeightKg = initialWeight) }

        var didSeedInitialWeight = false
        weightTrackingRepository.observeLatestEntry()
            .onEach { latest ->
                if (latest == null && initialWeight > 0f && !didSeedInitialWeight) {
                    didSeedInitialWeight = true
                    viewModelScope.launch(Dispatchers.IO) {
                        weightTrackingRepository.saveDailyWeight(initialWeight)
                    }
                }
                val latestWeight = latest?.weightKg ?: initialWeight
                setState {
                    copy(
                        initialWeightKg = if (initialWeightKg <= 0f && latestWeight > 0f) latestWeight else initialWeightKg,
                        currentWeightKg = latestWeight,
                        draftWeightKg = if (draftWeightKg <= 0f) latestWeight else draftWeightKg
                    )
                }
                if (initialWeight <= 0f && latestWeight > 0f) {
                    userProfileSharePref.saveInitialWeightKg(latestWeight.toInt())
                }
            }
            .launchIn(viewModelScope)

        combine(selectedTabFlow, periodOffsetFlow) { tab, offset ->
            val range = buildRange(tab, offset)
            Triple(tab, range, offset)
        }.flatMapLatest { (tab, range, offset) ->
            weightTrackingRepository.observeEntriesInRange(
                startMillis = range.startMillis,
                endMillis = range.endMillis
            ).onEach { entries ->
                val chart = buildChart(tab, entries, range)
                setState {
                    copy(
                        selectedTab = tab,
                        periodOffset = offset,
                        periodTitle = range.title,
                        chartData = chart.points,
                        xAxisLabels = chart.xAxisLabels,
                        yMin = chart.yMin,
                        yMax = chart.yMax
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun selectTab(tab: WeightReportTab) {
        if (selectedTabFlow.value == tab) return
        selectedTabFlow.value = tab
        periodOffsetFlow.value = 0
    }

    fun nextPeriod() {
        periodOffsetFlow.value = periodOffsetFlow.value + 1
    }

    fun prevPeriod() {
        periodOffsetFlow.value = periodOffsetFlow.value - 1
    }

    fun openAddWeightSheet() = setState {
        copy(
            showAddWeightSheet = true,
            draftWeightKg = if (currentWeightKg > 0f) currentWeightKg else initialWeightKg
        )
    }

    fun closeAddWeightSheet() = setState {
        copy(showAddWeightSheet = false)
    }

    fun updateDraftWeight(weightKg: Float) = setState {
        copy(draftWeightKg = weightKg.coerceIn(25f, 200f))
    }

    fun saveDraftWeight() {
        var valueToSave = 0f
        getState { state -> valueToSave = state.draftWeightKg }
        if (valueToSave <= 0f) return
        viewModelScope.launch(Dispatchers.IO) {
            weightTrackingRepository.saveDailyWeight(valueToSave)
        }
        setState {
            copy(
                currentWeightKg = valueToSave,
                initialWeightKg = if (initialWeightKg <= 0f) valueToSave else initialWeightKg,
                showAddWeightSheet = false
            )
        }
    }
}

private data class ReportRange(
    val startMillis: Long,
    val endMillis: Long,
    val title: String,
    val monthDays: Int = 0,
)

private data class ChartResult(
    val points: List<WeightChartPoint>,
    val xAxisLabels: List<WeightAxisLabel>,
    val yMin: Float,
    val yMax: Float,
)

private fun buildRange(tab: WeightReportTab, offset: Int): ReportRange {
    val calendar = Calendar.getInstance()
    return when (tab) {
        WeightReportTab.MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.MONTH, offset)
            val start = calendar.timeInMillis
            val days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthNumber = calendar.get(Calendar.MONTH) + 1
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            ReportRange(
                startMillis = start,
                endMillis = calendar.timeInMillis,
                title = "tháng $monthNumber",
                monthDays = days
            )
        }

        WeightReportTab.YEAR -> {
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.YEAR, offset)
            val start = calendar.timeInMillis
            val year = calendar.get(Calendar.YEAR)
            calendar.add(Calendar.YEAR, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            ReportRange(
                startMillis = start,
                endMillis = calendar.timeInMillis,
                title = "năm $year"
            )
        }
    }
}

private fun buildChart(
    tab: WeightReportTab,
    entries: List<WeightEntryEntity>,
    range: ReportRange
): ChartResult {
    val points = when (tab) {
        WeightReportTab.MONTH -> buildMonthPoints(entries, range.monthDays)
        WeightReportTab.YEAR -> buildYearPoints(entries)
    }

    val labels = when (tab) {
        WeightReportTab.MONTH -> buildMonthLabels(range.monthDays)
        WeightReportTab.YEAR -> buildYearLabels()
    }

    if (points.isEmpty()) {
        return ChartResult(
            points = emptyList(),
            xAxisLabels = labels,
            yMin = 0f,
            yMax = 1f
        )
    }

    val minValue = points.minOf { it.value }
    val maxValue = points.maxOf { it.value }
    val margin = 1f
    return ChartResult(
        points = points,
        xAxisLabels = labels,
        yMin = max(0f, minValue - margin),
        yMax = max(minValue + margin, maxValue + margin)
    )
}

private fun buildMonthPoints(entries: List<WeightEntryEntity>, monthDays: Int): List<WeightChartPoint> {
    if (monthDays <= 0 || entries.isEmpty()) return emptyList()
    val latestByDay = entries.groupBy { dayOfMonth(it.dayStartMillis) }
        .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt }?.weightKg ?: 0f }
    if (latestByDay.isEmpty()) return emptyList()
    val denominator = max(1, monthDays - 1).toFloat()
    return latestByDay.entries
        .sortedBy { it.key }
        .map { (day, value) ->
            WeightChartPoint(
                xRatio = ((day - 1) / denominator).coerceIn(0f, 1f),
                value = value
            )
        }
}

private fun buildYearPoints(entries: List<WeightEntryEntity>): List<WeightChartPoint> {
    if (entries.isEmpty()) return emptyList()
    val latestByMonth = entries.groupBy { monthOfYear(it.dayStartMillis) }
        .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt }?.weightKg ?: 0f }
    return latestByMonth.entries
        .sortedBy { it.key }
        .map { (month, value) ->
            WeightChartPoint(
                xRatio = ((month - 1) / 11f).coerceIn(0f, 1f),
                value = value
            )
        }
}

private fun buildMonthLabels(days: Int): List<WeightAxisLabel> {
    if (days <= 0) return emptyList()
    val marks = mutableListOf(5, 10, 15, 20, 25).apply {
        if (days !in this) add(days)
    }.filter { it in 1..days }
        .distinct()
        .sorted()

    val denominator = max(1, days - 1).toFloat()
    return marks.map { day ->
        WeightAxisLabel(
            xRatio = ((day - 1) / denominator).coerceIn(0f, 1f),
            label = day.toString()
        )
    }
}

private fun buildYearLabels(): List<WeightAxisLabel> {
    val months = listOf(1, 3, 5, 7, 9, 11)
    return months.map { month ->
        WeightAxisLabel(
            xRatio = ((month - 1) / 11f).coerceIn(0f, 1f),
            label = "T$month"
        )
    }
}

private fun dayOfMonth(timeMillis: Long): Int {
    return Calendar.getInstance().apply { timeInMillis = timeMillis }.get(Calendar.DAY_OF_MONTH)
}

private fun monthOfYear(timeMillis: Long): Int {
    return Calendar.getInstance().apply { timeInMillis = timeMillis }.get(Calendar.MONTH) + 1
}
