package com.dialysis.app.sync

import com.dialysis.app.data.local.entity.PendingWaterDeleteEntity
import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.response.WaterIntakeResponse
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.telemetry.ApiEndpoint
import com.dialysis.app.telemetry.RecordPayload
import com.dialysis.app.telemetry.SyncDataType
import com.dialysis.app.telemetry.SyncFailure
import com.dialysis.app.telemetry.SyncFailureReporter
import com.dialysis.app.telemetry.SyncOperation
import java.time.Instant

/** Local water data the sync needs. Implemented by WaterTrackingRepository. */
interface WaterSyncLocalStore {
    suspend fun getUnsyncedEntries(): List<WaterEntryEntity>
    suspend fun markEntrySynced(localId: Long, syncedId: Long)
    suspend fun getPendingDeletes(): List<PendingWaterDeleteEntity>
    suspend fun removePendingDelete(id: Long)
}

interface WaterSyncApi {
    suspend fun create(request: WaterIntakeRequest): Result<WaterIntakeResponse>
    suspend fun delete(syncedId: Long): Result<Unit>
}

class NetworkWaterSyncApi(private val networkManager: NetworkManager) : WaterSyncApi {
    override suspend fun create(request: WaterIntakeRequest): Result<WaterIntakeResponse> =
        networkManager.resolve { networkManager.appServices.syncWaterIntake(request) }

    override suspend fun delete(syncedId: Long): Result<Unit> =
        networkManager.resolveNullable { networkManager.appServices.deleteWaterIntake(syncedId) }
            .map { }
}

/** Account state the sync needs. Implemented over AccountSharePref / UserProfileSharePref. */
interface WaterSyncAccount {
    fun isLoggedIn(): Boolean
    fun dailyWaterGoalMl(): Int
    fun setLastWaterSyncAt(timestampMillis: Long)
}

class SharePrefWaterSyncAccount(
    private val accountSharePref: AccountSharePref,
    private val userProfileSharePref: UserProfileSharePref,
) : WaterSyncAccount {
    override fun isLoggedIn(): Boolean = accountSharePref.getToken().isNotBlank()

    override fun dailyWaterGoalMl(): Int = userProfileSharePref.getDailyWaterGoalMl()

    override fun setLastWaterSyncAt(timestampMillis: Long) =
        accountSharePref.setLastWaterSyncAt(timestampMillis)
}

enum class SyncOutcome { SUCCESS, RETRY }

/**
 * Pushes unsynced water entries and pending deletes. Sync behavior is the same as before;
 * each failure is reported with its record payload, plus one `sync_completed` per run.
 */
class WaterIntakeSyncRunner(
    private val store: WaterSyncLocalStore,
    private val api: WaterSyncApi,
    private val account: WaterSyncAccount,
    private val reporter: SyncFailureReporter,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun run(attempt: Int): SyncOutcome {
        if (!account.isLoggedIn()) {
            reporter.reportSyncCompleted(SyncDataType.WATER, 0, 0, attempt)
            return SyncOutcome.SUCCESS
        }

        var successCount = 0
        var failedCount = 0
        val dailyGoalMl = account.dailyWaterGoalMl().coerceAtLeast(1)
        val unsyncedEntries = store.getUnsyncedEntries()
        val pendingDeletes = store.getPendingDeletes()
        val pendingCount = unsyncedEntries.size + pendingDeletes.size

        unsyncedEntries.forEach { entry ->
            val loggedAt = Instant.ofEpochMilli(entry.createdAt).toString()
            val request = WaterIntakeRequest(
                drinkName = entry.drinkName,
                rawAmount = entry.amountMl,
                weightRatio = entry.amountMl.toDouble() / dailyGoalMl.toDouble(),
                loggedAt = loggedAt
            )
            api.create(request).fold(
                onSuccess = { response ->
                    store.markEntrySynced(entry.id, response.id)
                    successCount++
                },
                onFailure = { error ->
                    failedCount++
                    reporter.reportSyncFailure(
                        SyncFailure(
                            dataType = SyncDataType.WATER,
                            operation = SyncOperation.CREATE,
                            endpoint = ApiEndpoint.WATER_CREATE,
                            error = error,
                            attempt = attempt,
                            recordId = entry.id.toString(),
                            recordCreatedAt = loggedAt,
                            pendingCount = pendingCount,
                            payload = RecordPayload.Water(entry.amountMl, entry.drinkName, loggedAt),
                        )
                    )
                }
            )
        }

        pendingDeletes.forEach { pending ->
            api.delete(pending.syncedId).fold(
                onSuccess = {
                    store.removePendingDelete(pending.id)
                    successCount++
                },
                onFailure = { error ->
                    failedCount++
                    reporter.reportSyncFailure(
                        SyncFailure(
                            dataType = SyncDataType.WATER,
                            operation = SyncOperation.DELETE,
                            endpoint = ApiEndpoint.WATER_DELETE,
                            error = error,
                            attempt = attempt,
                            recordId = pending.id.toString(),
                            pendingCount = pendingCount,
                        )
                    )
                }
            )
        }

        if (failedCount == 0 && successCount > 0) {
            account.setLastWaterSyncAt(clock())
        }
        reporter.reportSyncCompleted(SyncDataType.WATER, successCount, failedCount, attempt)
        return if (failedCount > 0) SyncOutcome.RETRY else SyncOutcome.SUCCESS
    }
}
