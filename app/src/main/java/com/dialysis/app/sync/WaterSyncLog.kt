package com.dialysis.app.sync

import android.util.Log
import com.dialysis.app.BuildConfig

/**
 * Diagnostic logcat output for the water sync and the silent re-login (tag `WaterSync`), debug builds
 * only. Callers must never pass a token, password or username.
 */
object WaterSyncLog {
    private const val TAG = "WaterSync"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }
}
