package com.dialysis.app.ui.weight

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

internal fun formatWeightValue(weight: Float): String {
    return if (abs(weight - weight.toInt()) < 0.05f) {
        weight.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", weight)
    }
}

internal fun buildProgressText(progressKg: Float): String {
    val rounded = ((progressKg * 10).toInt()) / 10f
    val body = formatWeightValue(abs(rounded))
    return when {
        rounded > 0f -> "+$body kg"
        rounded < 0f -> "-$body kg"
        else -> "0 kg"
    }
}

internal fun formatWeightHistoryDate(date: Date): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
}
