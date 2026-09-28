package com.dialysis.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class UrineSyncScheduler(
    private val context: Context
) {
    fun enqueue() {
        val workRequest = OneTimeWorkRequestBuilder<UrineSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
    }

    private companion object {
        private const val UNIQUE_WORK_NAME = "urine_sync"
    }
}
