package com.dialysis.app.ui.weight

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal fun formatWeightApiDate(timeMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(timeMillis)
}

internal fun parseWeightApiDate(value: String): Long? {
    return runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)?.time }.getOrNull()
}

internal fun parseWeightApiMonth(value: String): Int? {
    val date = runCatching { SimpleDateFormat("yyyy-MM", Locale.US).parse(value) }.getOrNull() ?: return null
    return Calendar.getInstance().apply { time = date }.get(Calendar.MONTH) + 1
}

internal fun parseWeightApiDateTime(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(value)?.time
    }.getOrNull() ?: parseWeightApiDate(value)
}

internal fun startOfWeightDay(timeMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

internal fun weightDayOfMonth(timeMillis: Long): Int {
    return Calendar.getInstance().apply { timeInMillis = timeMillis }.get(Calendar.DAY_OF_MONTH)
}

internal fun weightMonthOfYear(timeMillis: Long): Int {
    return Calendar.getInstance().apply { timeInMillis = timeMillis }.get(Calendar.MONTH) + 1
}
