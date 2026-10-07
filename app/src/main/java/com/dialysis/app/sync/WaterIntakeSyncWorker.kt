package com.dialysis.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaterIntakeSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val runner: WaterIntakeSyncRunner by inject()

    override suspend fun doWork(): Result = runner.run(attempt = runAttemptCount).toWorkerResult()
}

/** Same retry behavior as before the runners: any failed record means retry. */
internal fun SyncOutcome.toWorkerResult(): ListenableWorker.Result = when (this) {
    SyncOutcome.SUCCESS -> ListenableWorker.Result.success()
    SyncOutcome.RETRY -> ListenableWorker.Result.retry()
}
