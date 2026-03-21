package com.dialysis.app.data.local.model

import androidx.room.ColumnInfo

data class DailyTotal(
    @ColumnInfo(name = "day")
    val day: String,
    @ColumnInfo(name = "total_ml")
    val totalMl: Int,
)
