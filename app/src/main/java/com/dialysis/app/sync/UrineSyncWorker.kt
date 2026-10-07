package com.dialysis.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UrineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val runner: UrineSyncRunner by inject()

    override suspend fun doWork(): Result = runner.run(attempt = runAttemptCount).toWorkerResult()
}
