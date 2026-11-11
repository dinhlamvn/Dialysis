package com.dialysis.app.sync

import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.local.newWaterEntry

enum class RecordIntakeResult { SYNCED, PENDING, LOCAL_ONLY }

/**
 * Adds a drink, same flow as iOS `WaterRepository.recordIntake`:
 * - logged in and online: `POST mobile/water/intake`, then store the record as synced with its server id
 *   and trigger a sync (the pull refreshes local data, as iOS refetches after an online create);
 * - offline or the call fails for any reason, including 401 (user decision 2026-10-27): store as pending
 *   and trigger a sync, so the record is pushed through `/mobile/sync` on the next sync;
 * - guest: store locally only, no server call (unchanged guest behaviour).
 */
class WaterIntakeRecorder(
    private val repository: WaterTrackingRepository,
    private val remote: WaterSyncRemote,
    private val isLoggedIn: () -> Boolean,
    private val isOnline: () -> Boolean,
    private val requestSync: () -> Unit,
) {

    suspend fun record(drinkName: String, amountMl: Int, loggedAt: Long): RecordIntakeResult {
        val entry = newWaterEntry(drinkName = drinkName, amountMl = amountMl, createdAt = loggedAt)
        if (!isLoggedIn()) {
            repository.insertEntry(entry)
            return RecordIntakeResult.LOCAL_ONLY
        }
        if (isOnline()) {
            val result = remote.createIntake(entry.toWaterIntakeRequest())
            if (result is RemoteCreateResult.Created) {
                repository.insertEntry(entry.copy(syncedId = result.serverId))
                requestSync()
                return RecordIntakeResult.SYNCED
            }
        }
        repository.insertEntry(entry)
        requestSync()
        return RecordIntakeResult.PENDING
    }
}
