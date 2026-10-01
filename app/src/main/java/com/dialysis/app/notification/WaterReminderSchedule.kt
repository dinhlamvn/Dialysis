package com.dialysis.app.notification

import java.util.Calendar

object WaterReminderSchedule {
    private val reminderHours = listOf(6, 9, 12, 15, 18, 21)

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
        val isExpectedHour = hour in reminderHours
        return isExpectedHour && minute <= SLOT_TOLERANCE_MINUTES
    }

    private fun slotTimeMillis(baseMillis: Long, hour: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = baseMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private const val SLOT_TOLERANCE_MINUTES = 10
}
