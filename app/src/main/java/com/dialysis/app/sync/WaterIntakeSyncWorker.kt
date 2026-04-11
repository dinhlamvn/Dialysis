package com.dialysis.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

class WaterIntakeSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val waterTrackingRepository: WaterTrackingRepository by inject()
    private val networkManager: NetworkManager by inject()
    private val accountSharePref: AccountSharePref by inject()
    private val userProfileSharePref: UserProfileSharePref by inject()

    override suspend fun doWork(): Result {
        if (accountSharePref.getToken().isBlank()) return Result.success()

        var hasFailure = false
        val dailyGoalMl = userProfileSharePref.getDailyWaterGoalMl().coerceAtLeast(1)

        waterTrackingRepository.getUnsyncedEntries().forEach { entry ->
            val request = WaterIntakeRequest(
                drinkName = entry.drinkName,
                rawAmount = entry.amountMl,
                weightRatio = entry.amountMl.toDouble() / dailyGoalMl.toDouble(),
                loggedAt = Instant.ofEpochMilli(entry.createdAt).toString()
            )
            val result = networkManager.resolve {
                networkManager.appServices.syncWaterIntake(request)
            }
            if (result.isSuccess) {
                val syncedId = result.getOrNull()?.id ?: return@forEach
                waterTrackingRepository.markEntrySynced(entry.id, syncedId)
            } else {
                hasFailure = true
            }
        }

        waterTrackingRepository.getPendingDeletes().forEach { pending ->
            val result = networkManager.resolveNullable {
                networkManager.appServices.deleteWaterIntake(pending.syncedId)
            }
            if (result.isSuccess) {
                waterTrackingRepository.removePendingDelete(pending.id)
            } else {
                hasFailure = true
            }
        }

        return if (hasFailure) Result.retry() else Result.success()
    }
}
