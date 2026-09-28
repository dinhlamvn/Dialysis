package com.dialysis.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.UrineLogRequest
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UrineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val networkManager: NetworkManager by inject()
    private val accountSharePref: AccountSharePref by inject()
    private val userProfileSharePref: UserProfileSharePref by inject()

    override suspend fun doWork(): Result {
        if (accountSharePref.getToken().isBlank()) return Result.success()

        var hasFailure = false
        var didSyncData = false
        val syncedClientIds = mutableSetOf<String>()

        userProfileSharePref.getLocalUrineSamples().forEach { sample ->
            val request = UrineLogRequest(
                amount = sample.amountMl,
                loggedAt = sample.loggedAt,
                note = sample.note,
                clientId = sample.clientId
            )
            val result = networkManager.resolve {
                networkManager.appServices.logUrine(request)
            }
            if (result.isSuccess) {
                syncedClientIds += sample.clientId
                didSyncData = true
            } else {
                hasFailure = true
            }
        }

        if (syncedClientIds.isNotEmpty()) {
            userProfileSharePref.removeLocalUrineSamplesByClientIds(syncedClientIds)
        }

        return if (hasFailure) Result.retry() else Result.success()
    }
}
