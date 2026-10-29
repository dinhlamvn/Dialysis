package com.dialysis.app.sync

import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.local.newWaterEntry
import com.dialysis.app.data.network.request.MobileSyncItem
import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.response.MobileSyncResult
import com.dialysis.app.data.network.response.WaterIntakeResponse
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val MIN_RAW_AMOUNT = 1
private const val MAX_RAW_AMOUNT = 5000

/** ISO-8601 in UTC with milliseconds, e.g. `2026-10-29T14:29:12.889Z`. */
private val ISO_UTC_MILLIS: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).withZone(ZoneOffset.UTC)

/**
 * Formats [epochMillis] in UTC, independent of the device time zone, like iOS
 * (`ISO8601DateFormatter` with fractional seconds, GMT). The server appears to ignore a non-UTC offset: a
 * `+09:00` time was stored 9h late and moved late-evening records to the next day.
 */
fun formatIsoUtc(epochMillis: Long): String = ISO_UTC_MILLIS.format(Instant.ofEpochMilli(epochMillis))

/** Water intake payload for one local record (iOS `WaterSyncEngine.buildIntakeRequest`); times in UTC. */
fun WaterEntryEntity.toWaterIntakeRequest(): WaterIntakeRequest =
    WaterIntakeRequest(
        drinkName = drinkName,
        rawAmount = amountMl.coerceIn(MIN_RAW_AMOUNT, MAX_RAW_AMOUNT),
        weightRatio = weightRatio.coerceIn(0.0, 1.0),
        loggedAt = formatIsoUtc(createdAt)
    )

/** `/mobile/sync` items for pending water records (iOS `WaterSyncEngine.pushPendingIntakes`); times in UTC. */
fun buildWaterSyncItems(entries: List<WaterEntryEntity>): List<MobileSyncItem> = entries.map { entry ->
    MobileSyncItem(
        type = MobileSyncItem.TYPE_WATER_INTAKE,
        clientId = entry.clientId,
        data = entry.toWaterIntakeRequest(),
        createdAtLocal = formatIsoUtc(entry.createdAtLocal)
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

/**
 * Parses server timestamps with any offset (`Z`, `+00:00`, `+07:00`, ...), including the UTC strings
 * from [formatIsoUtc]; null when the format is unknown.
 */
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
