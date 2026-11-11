package com.dialysis.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Runs [WaterSyncEngine]; WorkManager retries with backoff when any step fails. */
class WaterIntakeSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val waterSyncEngine: WaterSyncEngine by inject()

    override suspend fun doWork(): Result {
        return when (waterSyncEngine.runSyncSequence()) {
            WaterSyncOutcome.FAILED -> Result.retry()
            WaterSyncOutcome.SKIPPED,
            WaterSyncOutcome.SUCCESS -> Result.success()
        }
    }
}
