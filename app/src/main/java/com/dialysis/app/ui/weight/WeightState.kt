package com.dialysis.app.ui.weight

import com.dialysis.app.base.BaseState

data class WeightState(
    val initialWeightKg: Float = 0f,
    val currentWeightKg: Float = 0f,
    val selectedTab: WeightReportTab = WeightReportTab.MONTH,
    val periodOffset: Int = 0,
    val periodTitle: String = "",
    val showAddWeightSheet: Boolean = false,
    val draftWeightKg: Float = 0f,
    val editingMode: WeightEditingMode = WeightEditingMode.CURRENT,
    val isSavingWeight: Boolean = false,
    val chartData: List<WeightChartPoint> = emptyList(),
    val xAxisLabels: List<WeightAxisLabel> = emptyList(),
    val yMin: Float = 0f,
    val yMax: Float = 1f,
) : BaseState

enum class WeightReportTab {
    MONTH,
    YEAR
}

enum class WeightEditingMode {
    INITIAL,
    CURRENT
}

data class WeightChartPoint(
    val xRatio: Float,
    val value: Float,
)

data class WeightAxisLabel(
    val xRatio: Float,
    val label: String,
)
