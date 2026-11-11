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
 * [log] receives diagnostic lines (record ids, amounts, times; never credentials).
 */
class WaterSyncEngine(
    private val repository: WaterTrackingRepository,
    private val remote: WaterSyncRemote,
    private val isLoggedIn: () -> Boolean,
    private val onSyncCompleted: (Long) -> Unit = {},
    private val clock: () -> Long = System::currentTimeMillis,
    private val log: (String) -> Unit = {},
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
        pending.forEach {
            log("push pending: client_id=${it.clientId} amount=${it.amountMl} logged_at=${it.createdAt}")
        }
        val response = remote.syncOffline(MobileSyncRequest(buildWaterSyncItems(pending)))
            .getOrElse {
                log("sync failed: ${it.message}")
                return false
            }
        val results = response.results.orEmpty()
        results.forEach {
            log("sync result: client_id=${it.clientId} success=${it.success} server_id=${it.serverId}")
        }
        val synced = mapWaterSyncResults(pending, results)
        if (!isLoggedIn()) return false
        log("mark synced (local_id -> server_id): $synced")
        repository.markEntriesSynced(synced)
        return synced.size == pending.size
    }

    private suspend fun pullAndMerge(): Boolean {
        val history = mutableListOf<WaterIntakeResponse>()
        var isComplete = false
        for (page in 1..MAX_HISTORY_PAGES) {
            if (!isLoggedIn()) return false
            val items = remote.getHistoryPage(page).getOrElse {
                log("history pull failed at page $page: ${it.message}")
                return false
            }
            if (items.isEmpty()) {
                isComplete = true
                break
            }
            history += items
        }
        if (!isLoggedIn()) return false
        log("history pull: count=${history.size} complete=$isComplete server_ids=${history.map { it.id }}")
        val blockedServerIds = repository.getPendingDeletes().map { it.syncedId }.toSet()
        val now = clock()
        val removed = repository.mergeServerHistory(
            history = history.map { it.toSyncedEntity(now) },
            blockedServerIds = blockedServerIds,
            isCompleteHistory = isComplete
        )
        removed.forEach {
            log("removed stale: local_id=${it.id} server_id=${it.syncedId} amount=${it.amountMl} created_at=${it.createdAt}")
        }
        return true
    }

    private companion object {
        const val MAX_HISTORY_PAGES = 500
    }
}
