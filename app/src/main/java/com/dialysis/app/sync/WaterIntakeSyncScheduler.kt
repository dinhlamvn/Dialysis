package com.dialysis.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class WaterIntakeSyncScheduler(
    private val context: Context
) {
    fun enqueue() {
        val workRequest = OneTimeWorkRequestBuilder<WaterIntakeSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

