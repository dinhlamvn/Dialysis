package com.dialysis.app.sync

import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.data.network.request.MobileSyncItem
import com.dialysis.app.data.network.request.WeightLogRequest
import com.dialysis.app.data.network.response.MobileSyncResult
import com.dialysis.app.ui.weight.formatWeightApiDate

/** Stable for one saved value; the Room id is not used because it can repeat after a reinstall. */
fun weightClientId(entry: WeightEntryEntity): String = "weight_${entry.dayStartMillis}_${entry.updatedAt}"

/** `/mobile/sync` items for pending weights: `date` is the entry's own day, not the sync time. */
fun buildWeightSyncItems(entries: List<WeightEntryEntity>): List<MobileSyncItem> = entries.map { entry ->
    MobileSyncItem(
        type = MobileSyncItem.TYPE_WEIGHT_LOG,
        clientId = weightClientId(entry),
        data = WeightLogRequest(
            // Via text: 65.1f.toDouble() would serialize as 65.0999984...
            weight = entry.weightKg.toString().toDouble(),
            date = formatWeightApiDate(entry.dayStartMillis),
            note = entry.note
        ),
        createdAtLocal = formatIsoUtc(entry.updatedAt)
    )
}

/**
 * Maps local entry id -> server id for every pending entry whose result is a successful
 * `weight_log` with a server id. Failed, missing or unknown results are left out (stay pending).
 */
fun mapWeightSyncResults(
    pending: List<WeightEntryEntity>,
    results: List<MobileSyncResult>
): Map<Long, Long> {
    val localIdByClientId = pending.associate { weightClientId(it) to it.id }
    return results
        .asSequence()
        .filter { it.type == MobileSyncItem.TYPE_WEIGHT_LOG && it.success }
        .mapNotNull { result ->
            val localId = localIdByClientId[result.clientId] ?: return@mapNotNull null
            val serverId = result.serverId ?: return@mapNotNull null
            localId to serverId
        }
        .toMap()
}
