package com.dialysis.app.notification

import java.util.Calendar

object WaterReminderSchedule {
    private val reminderHours = listOf(6, 8, 10, 12, 14, 16, 18, 20, 22, 24)

    fun nextTriggerAfter(nowMillis: Long = System.currentTimeMillis()): Long {
        reminderHours.forEach { hour ->
            val candidate = slotTimeMillis(nowMillis, hour)
            if (candidate > nowMillis) return candidate
        }
        return Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, reminderHours.first())
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun isReminderSlot(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val isExpectedHour = hour == 0 || (hour in 6..22 && hour % 2 == 0)
        return isExpectedHour && minute <= SLOT_TOLERANCE_MINUTES
    }

    private fun slotTimeMillis(baseMillis: Long, hour: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = baseMillis
            if (hour == 24) {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, hour)
            }
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private const val SLOT_TOLERANCE_MINUTES = 10
}
