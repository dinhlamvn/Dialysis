package com.dialysis.app.notification

import android.content.Context

class WaterReminderMessageRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val selector = WaterReminderMessageSelector()

    fun nextMessage(messages: Array<String>): String {
        val savedRound = WaterReminderMessageRound(
            order = prefs.getString(KEY_MESSAGE_ORDER, null)?.toIndexList().orEmpty(),
            position = prefs.getInt(KEY_MESSAGE_POSITION, 0)
        )
        val result = selector.next(messages = messages.toList(), savedRound = savedRound)
        prefs.edit()
            .putString(KEY_MESSAGE_ORDER, result.nextRound.order.joinToString(","))
            .putInt(KEY_MESSAGE_POSITION, result.nextRound.position)
            .apply()
        return result.message
    }

    private fun String.toIndexList(): List<Int>? {
        if (isBlank()) return null
        return split(",").map { it.toIntOrNull() ?: return null }
    }

    private companion object {
        const val PREFS_NAME = "water_reminder_pref"
        const val KEY_MESSAGE_ORDER = "message_order"
        const val KEY_MESSAGE_POSITION = "message_position"
    }
}
