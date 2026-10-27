package com.dialysis.app.sync

import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.local.newWaterEntry
import com.dialysis.app.data.network.request.MobileSyncItem
import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.response.MobileSyncResult
import com.dialysis.app.data.network.response.WaterIntakeResponse
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val MIN_RAW_AMOUNT = 1
private const val MAX_RAW_AMOUNT = 5000

/** ISO-8601 with milliseconds and the zone offset, e.g. `2026-10-27T08:30:00.000+07:00`. */
private val ISO_OFFSET_MILLIS: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

fun formatIsoOffset(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    ISO_OFFSET_MILLIS.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

/** Water intake payload for one local record (iOS `WaterSyncEngine.buildIntakeRequest`). */
fun WaterEntryEntity.toWaterIntakeRequest(zone: ZoneId = ZoneId.systemDefault()): WaterIntakeRequest =
    WaterIntakeRequest(
        drinkName = drinkName,
        rawAmount = amountMl.coerceIn(MIN_RAW_AMOUNT, MAX_RAW_AMOUNT),
        weightRatio = weightRatio.coerceIn(0.0, 1.0),
        loggedAt = formatIsoOffset(createdAt, zone)
    )

/** `/mobile/sync` items for pending water records (iOS `WaterSyncEngine.pushPendingIntakes`). */
fun buildWaterSyncItems(
    entries: List<WaterEntryEntity>,
    zone: ZoneId = ZoneId.systemDefault()
): List<MobileSyncItem> = entries.map { entry ->
    MobileSyncItem(
        type = MobileSyncItem.TYPE_WATER_INTAKE,
        clientId = entry.clientId,
        data = entry.toWaterIntakeRequest(zone),
        createdAtLocal = formatIsoOffset(entry.createdAtLocal, zone)
    )
}

/**
 * Maps local record id -> server id for every pending record whose result is a successful
 * `water_intake` with a server id. Failed, missing or unknown results are left out (stay pending).
 */
fun mapWaterSyncResults(
    pending: List<WaterEntryEntity>,
    results: List<MobileSyncResult>
): Map<Long, Long> {
    val localIdByClientId = pending.associate { it.clientId to it.id }
    return results
        .asSequence()
        .filter { it.type == MobileSyncItem.TYPE_WATER_INTAKE && it.success }
        .mapNotNull { result ->
            val localId = localIdByClientId[result.clientId] ?: return@mapNotNull null
            val serverId = result.serverId ?: return@mapNotNull null
            localId to serverId
        }
        .toMap()
}

/** Parses server timestamps with an offset (`+07:00` or `Z`); null when the format is unknown. */
fun parseServerTime(value: String): Long? =
    runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }
        .recoverCatching { Instant.parse(value).toEpochMilli() }
        .getOrNull()

/** Local synced record for a server history item; keeps the server's weight_ratio. */
fun WaterIntakeResponse.toSyncedEntity(now: Long = System.currentTimeMillis()): WaterEntryEntity =
    newWaterEntry(
        drinkName = drinkName,
        amountMl = rawAmount,
        createdAt = parseServerTime(loggedAt) ?: now,
        syncedId = id,
        weightRatio = weightRatio,
        createdAtLocal = parseServerTime(createdAt) ?: now
    )
