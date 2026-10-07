package com.dialysis.app.telemetry

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/** Firebase-backed [AppTelemetry]. Telemetry must never crash the app, so every call is guarded. */
class FirebaseAppTelemetry(context: Context) : AppTelemetry {

    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    init {
        analytics.setAnalyticsCollectionEnabled(true)
        crashlytics.setCrashlyticsCollectionEnabled(true)
    }

    override fun logEvent(name: String, params: Map<String, Any>) = guarded {
        analytics.logEvent(name, params.toBundle())
    }

    override fun recordNonFatal(
        throwable: Throwable,
        customKeys: Map<String, Any>,
        logLine: String,
    ) = guarded {
        customKeys.forEach { (key, value) -> crashlytics.setCustomKeyValue(key, value) }
        crashlytics.log(logLine)
        crashlytics.recordException(throwable)
    }

    override fun setUserId(userId: String) = guarded {
        crashlytics.setUserId(userId)
        analytics.setUserId(userId)
    }

    override fun setCustomKey(key: String, value: String) = guarded {
        crashlytics.setCustomKey(key, value)
    }

    private fun FirebaseCrashlytics.setCustomKeyValue(key: String, value: Any) {
        when (value) {
            is Int -> setCustomKey(key, value)
            is Long -> setCustomKey(key, value)
            is Double -> setCustomKey(key, value)
            else -> setCustomKey(key, value.toString())
        }
    }

    private fun Map<String, Any>.toBundle(): Bundle = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                else -> putString(key, value.toString())
            }
        }
    }

    private inline fun guarded(block: () -> Unit) {
        runCatching(block)
    }

    companion object {
        fun isFirebaseReady(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()
    }
}
