package com.dialysis.app.ui.daily

import com.dialysis.app.base.BaseState

data class DailyDrinkItemState(
    val id: Long,
    val title: String,
    val subtitle: String,
    val time: String,
)

data class DailyReportState(
    val selectedDateLabel: String = "",
    val drinks: List<DailyDrinkItemState> = emptyList(),
    val progress: Float = 0f,
    val progressText: String = "0%",
    val showDrinkListSheet: Boolean = false,
    val selectedDrinkName: String? = null,
    val showDeleteDialog: Boolean = false,
    val deletingDrinkId: Long? = null,
    val deletingDrinkTitle: String = "",
) : BaseState
