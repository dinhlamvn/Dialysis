package com.dialysis.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object WaterReminderScheduler {
    private const val REQUEST_CODE_REMINDER = 1107
    private const val INTERVAL_MILLIS = 2L * 60L * 60L * 1000L

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = reminderPendingIntent(context)
        val firstTriggerAt = nextReminderTimeMillis()

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            firstTriggerAt,
            INTERVAL_MILLIS,
            pendingIntent
        )
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WaterReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextReminderTimeMillis(): Long {
        val now = Calendar.getInstance()
        val next = now.clone() as Calendar
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)

        for (hour in REMINDER_HOURS) {
            next.set(Calendar.HOUR_OF_DAY, hour)
            next.set(Calendar.MINUTE, 0)
            if (next.after(now)) return next.timeInMillis
        }

        next.add(Calendar.DAY_OF_MONTH, 1)
        next.set(Calendar.HOUR_OF_DAY, REMINDER_HOURS.first())
        next.set(Calendar.MINUTE, 0)
        return next.timeInMillis
    }

    private val REMINDER_HOURS = listOf(0, 6, 8, 10, 12, 14, 16, 18, 20, 22)
}
