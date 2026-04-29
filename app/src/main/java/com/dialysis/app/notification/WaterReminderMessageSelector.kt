package com.dialysis.app.notification

import kotlin.random.Random

class WaterReminderMessageSelector {
    fun next(
        messages: List<String>,
        savedRound: WaterReminderMessageRound
    ): WaterReminderMessageResult {
        require(messages.isNotEmpty()) { "messages must not be empty" }

        val activeRound = savedRound.takeIf { it.isValidFor(messages.size) }
            ?: WaterReminderMessageRound(order = shuffledIndexes(messages.size), position = 0)
        val message = messages[activeRound.order[activeRound.position]]
        val nextPosition = activeRound.position + 1
        val nextRound = if (nextPosition >= activeRound.order.size) {
            WaterReminderMessageRound(order = shuffledIndexes(messages.size), position = 0)
        } else {
            activeRound.copy(position = nextPosition)
        }
        return WaterReminderMessageResult(message = message, nextRound = nextRound)
    }

    private fun shuffledIndexes(size: Int): List<Int> {
        return List(size) { it }.shuffled(Random(System.currentTimeMillis()))
    }
}

data class WaterReminderMessageRound(
    val order: List<Int>,
    val position: Int
) {
    fun isValidFor(messageCount: Int): Boolean {
        return order.size == messageCount &&
            position in order.indices &&
            order.toSet().size == messageCount &&
            order.all { it in 0 until messageCount }
    }
}

data class WaterReminderMessageResult(
    val message: String,
    val nextRound: WaterReminderMessageRound
)
