package com.dialysis.app.sync

import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.network.request.MobileSyncRequest
import com.dialysis.app.data.network.response.WaterIntakeResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class WaterSyncOutcome { SKIPPED, SUCCESS, FAILED }

/**
 * Water sync, same order as iOS `WaterSyncEngine.runSyncSequence`:
 * 1. pending deletes (`DELETE mobile/water/intake/{id}`, "not found" counts as done),
 * 2. pending creates in one `POST mobile/sync` batch, matched back by `client_id`,
 * 3. pull server history and merge it into synced rows (pending rows are never dropped).
 * Runs are serialized so two triggers never push the same records at once.
 * [runSyncSequence] with `pendingCreatedBetween` pushes only pending records logged in that range
 * (Settings "sync today/week/month"); deletes and the pull always run in full.
 */
class WaterSyncEngine(
    private val repository: WaterTrackingRepository,
    private val remote: WaterSyncRemote,
    private val isLoggedIn: () -> Boolean,
    private val onSyncCompleted: (Long) -> Unit = {},
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()

    suspend fun runSyncSequence(pendingCreatedBetween: LongRange? = null): WaterSyncOutcome = mutex.withLock {
        if (!isLoggedIn()) return@withLock WaterSyncOutcome.SKIPPED
        val deletesOk = pushPendingDeletes()
        val createsOk = pushPendingCreates(pendingCreatedBetween)
        val pullOk = pullAndMerge()
        if (deletesOk && createsOk && pullOk) {
            onSyncCompleted(clock())
            WaterSyncOutcome.SUCCESS
        } else {
            WaterSyncOutcome.FAILED
        }
    }

    private suspend fun pushPendingDeletes(): Boolean {
        var ok = true
        repository.getPendingDeletes().forEach { pending ->
            when (remote.deleteIntake(pending.syncedId)) {
                RemoteDeleteResult.DELETED,
                RemoteDeleteResult.NOT_FOUND -> repository.removePendingDelete(pending.id)
                RemoteDeleteResult.FAILED -> ok = false
            }
        }
        return ok
    }

    private suspend fun pushPendingCreates(createdBetween: LongRange?): Boolean {
        val pending = if (createdBetween == null) {
            repository.getUnsyncedEntries()
        } else {
            repository.getUnsyncedEntriesBetween(createdBetween.first, createdBetween.last)
        }
        if (pending.isEmpty()) return true
        val response = remote.syncOffline(MobileSyncRequest(buildWaterSyncItems(pending)))
            .getOrElse { return false }
        val synced = mapWaterSyncResults(pending, response.results.orEmpty())
        if (!isLoggedIn()) return false
        repository.markEntriesSynced(synced)
        return synced.size == pending.size
    }

    private suspend fun pullAndMerge(): Boolean {
        val history = mutableListOf<WaterIntakeResponse>()
        var isComplete = false
        for (page in 1..MAX_HISTORY_PAGES) {
            if (!isLoggedIn()) return false
            val items = remote.getHistoryPage(page).getOrElse { return false }
            if (items.isEmpty()) {
                isComplete = true
                break
            }
            history += items
        }
        if (!isLoggedIn()) return false
        val blockedServerIds = repository.getPendingDeletes().map { it.syncedId }.toSet()
        val now = clock()
        repository.mergeServerHistory(
            history = history.map { it.toSyncedEntity(now) },
            blockedServerIds = blockedServerIds,
            isCompleteHistory = isComplete
        )
        return true
    }

    private companion object {
        const val MAX_HISTORY_PAGES = 500
    }
}
