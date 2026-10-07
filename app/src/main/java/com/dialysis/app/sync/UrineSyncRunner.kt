package com.dialysis.app.sync

import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.UrineLogRequest
import com.dialysis.app.sharepref.LocalUrineSample
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.telemetry.ApiEndpoint
import com.dialysis.app.telemetry.RecordPayload
import com.dialysis.app.telemetry.SyncDataType
import com.dialysis.app.telemetry.SyncFailure
import com.dialysis.app.telemetry.SyncFailureReporter
import com.dialysis.app.telemetry.SyncOperation

interface UrineSyncLocalStore {
    fun getLocalUrineSamples(): List<LocalUrineSample>
    fun removeLocalUrineSamplesByClientIds(clientIds: Set<String>)
}

class SharePrefUrineSyncLocalStore(
    private val userProfileSharePref: UserProfileSharePref,
) : UrineSyncLocalStore {
    override fun getLocalUrineSamples(): List<LocalUrineSample> =
        userProfileSharePref.getLocalUrineSamples()

    override fun removeLocalUrineSamplesByClientIds(clientIds: Set<String>) =
        userProfileSharePref.removeLocalUrineSamplesByClientIds(clientIds)
}

interface UrineSyncApi {
    suspend fun upload(request: UrineLogRequest): Result<Unit>
}

class NetworkUrineSyncApi(private val networkManager: NetworkManager) : UrineSyncApi {
    override suspend fun upload(request: UrineLogRequest): Result<Unit> =
        networkManager.resolve { networkManager.appServices.logUrine(request) }.map { }
}

/**
 * Uploads locally stored urine samples. Sync behavior is the same as before; each failed upload is
 * reported with its record payload, plus one `sync_completed` per run.
 */
class UrineSyncRunner(
    private val store: UrineSyncLocalStore,
    private val api: UrineSyncApi,
    private val isLoggedIn: () -> Boolean,
    private val reporter: SyncFailureReporter,
) {

    suspend fun run(attempt: Int): SyncOutcome {
        if (!isLoggedIn()) {
            reporter.reportSyncCompleted(SyncDataType.URINE, 0, 0, attempt)
            return SyncOutcome.SUCCESS
        }

        var successCount = 0
        var failedCount = 0
        val syncedClientIds = mutableSetOf<String>()
        val samples = store.getLocalUrineSamples()

        samples.forEach { sample ->
            val request = UrineLogRequest(
                amount = sample.amountMl,
                loggedAt = sample.loggedAt,
                note = sample.note,
                clientId = sample.clientId
            )
            api.upload(request).fold(
                onSuccess = {
                    syncedClientIds += sample.clientId
                    successCount++
                },
                onFailure = { error ->
                    failedCount++
                    reporter.reportSyncFailure(
                        SyncFailure(
                            dataType = SyncDataType.URINE,
                            operation = SyncOperation.CREATE,
                            endpoint = ApiEndpoint.URINE_CREATE,
                            error = error,
                            attempt = attempt,
                            recordId = sample.clientId,
                            recordCreatedAt = sample.createdAt,
                            pendingCount = samples.size,
                            payload = RecordPayload.Urine(
                                amountMl = sample.amountMl,
                                note = sample.note,
                                loggedAt = sample.loggedAt,
                                clientId = sample.clientId,
                            ),
                        )
                    )
                }
            )
        }

        if (syncedClientIds.isNotEmpty()) {
            store.removeLocalUrineSamplesByClientIds(syncedClientIds)
        }
        reporter.reportSyncCompleted(SyncDataType.URINE, successCount, failedCount, attempt)
        return if (failedCount > 0) SyncOutcome.RETRY else SyncOutcome.SUCCESS
    }
}
