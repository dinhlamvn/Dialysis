package com.dialysis.app.ui.weight

import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.data.network.response.WeightChartStats
import com.dialysis.app.data.network.response.WeightHistoryItem

internal fun WeightHistoryItem.toEntity(): WeightEntryEntity? {
    val dateText = loggedDate ?: date ?: return null
    val millis = parseWeightApiDate(dateText) ?: return null
    val weight = weight?.toFloat() ?: return null
    return WeightEntryEntity(
        serverId = id,
        weightKg = weight,
        dayStartMillis = startOfWeightDay(millis),
        note = note.orEmpty(),
        updatedAt = parseWeightApiDateTime(updatedAt ?: createdAt) ?: System.currentTimeMillis()
    )
}

internal fun WeightEntryEntity.toHistoryRow(): WeightHistoryRow {
    return WeightHistoryRow(
        localId = id,
        serverId = serverId,
        weightKg = weightKg,
        dateMillis = dayStartMillis,
        note = note,
    )
}

internal fun WeightHistoryRow.toEntity(): WeightEntryEntity {
    return WeightEntryEntity(
        id = localId,
        serverId = serverId,
        weightKg = weightKg,
        dayStartMillis = dateMillis,
        note = note,
        updatedAt = System.currentTimeMillis()
    )
}

internal fun WeightChartStats.toUi(): WeightChartStatsUi {
    return WeightChartStatsUi(
        min = min?.toFloat(),
        max = max?.toFloat(),
        avg = avg?.toFloat()
    )
}
