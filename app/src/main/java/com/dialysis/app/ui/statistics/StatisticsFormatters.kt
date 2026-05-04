package com.dialysis.app.ui.statistics

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val vietnameseLocale: Locale = Locale.forLanguageTag("vi-VN")

internal fun formatMl(valueMl: Int): String {
    return if (valueMl >= 1000) {
        String.format("%.1f l", valueMl / 1000f)
    } else {
        "$valueMl ml"
    }
}

internal fun dayLabel(timeMillis: Long): String {
    return SimpleDateFormat("EEE", vietnameseLocale).format(timeMillis)
}

internal fun monthYearTitle(timeMillis: Long): String {
    return SimpleDateFormat("MMMM yyyy", vietnameseLocale)
        .format(timeMillis)
        .replaceFirstChar { it.titlecase(vietnameseLocale) }
}

internal fun listDayTitle(timeMillis: Long): String {
    val calendar = Calendar.getInstance()
    val todayStart = startOfDay(System.currentTimeMillis())
    val dayStart = startOfDay(timeMillis)
    val yesterdayStart = Calendar.getInstance().apply {
        timeInMillis = todayStart
        add(Calendar.DAY_OF_MONTH, -1)
    }.timeInMillis

    return when (dayStart) {
        todayStart -> "Hôm nay"
        yesterdayStart -> "Hôm qua"
        else -> {
            calendar.timeInMillis = timeMillis
            "${calendar.get(Calendar.DAY_OF_MONTH)} tháng ${calendar.get(Calendar.MONTH) + 1}"
        }
    }
}
