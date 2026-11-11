package com.dialysis.app.sync

import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.MobileSyncRequest
import com.dialysis.app.data.network.response.MobileSyncResponseData
import java.util.concurrent.atomic.AtomicBoolean

interface WeightSyncRemote {
    suspend fun syncOffline(request: MobileSyncRequest): Result<MobileSyncResponseData>
}

/**
 * Uses [NetworkManager.appServices], not `syncAppServices`: a 401 here must not start a silent
 * re-login or raise the session-expired dialog.
 */
class NetworkWeightSyncRemote(private val networkManager: NetworkManager) : WeightSyncRemote {
    override suspend fun syncOffline(request: MobileSyncRequest): Result<MobileSyncResponseData> =
        networkManager.resolve { networkManager.appServices.syncOffline(request) }
}

/**
 * Silently pushes pending weights (`serverId == null`) in one `POST mobile/sync`. A trigger that
 * arrives while a sync runs is dropped, not queued. Anything not confirmed stays pending for the next run.
 */
class WeightSyncEngine(
    private val repository: WeightTrackingRepository,
    private val remote: WeightSyncRemote,
    private val isLoggedIn: () -> Boolean,
    private val isOnline: () -> Boolean,
) {
    private val running = AtomicBoolean(false)

    suspend fun syncPending() {
        if (!isLoggedIn() || !isOnline()) return
        if (!running.compareAndSet(false, true)) return
        try {
            pushPending()
        } finally {
            running.set(false)
        }
    }

    private suspend fun pushPending() {
        val pending = repository.getPendingEntries()
        if (pending.isEmpty()) return
        val response = remote.syncOffline(MobileSyncRequest(buildWeightSyncItems(pending)))
            .getOrElse { return }
        if (!isLoggedIn()) return
        val serverIds = mapWeightSyncResults(pending, response.results.orEmpty())
        pending.forEach { entry ->
            serverIds[entry.id]?.let { repository.markSynced(entry, it) }
        }
    }
}
