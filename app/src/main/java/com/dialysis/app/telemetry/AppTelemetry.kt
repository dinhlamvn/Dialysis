package com.dialysis.app.telemetry

/**
 * Single entry point for crash reporting and analytics. Firebase is only touched by
 * [FirebaseAppTelemetry]; everything else depends on this interface.
 *
 * Param and key values must be [String], [Int], [Long] or [Double].
 */
interface AppTelemetry {
    fun logEvent(name: String, params: Map<String, Any>)

    /** Sets [customKeys], writes [logLine] to the crash log, then records [throwable] as a non-fatal. */
    fun recordNonFatal(throwable: Throwable, customKeys: Map<String, Any>, logLine: String)

    fun setUserId(userId: String)

    fun setCustomKey(key: String, value: String)
}

/** Used when Firebase is not initialized (e.g. no google-services.json) so the app keeps working. */
object NoOpAppTelemetry : AppTelemetry {
    override fun logEvent(name: String, params: Map<String, Any>) = Unit

    override fun recordNonFatal(throwable: Throwable, customKeys: Map<String, Any>, logLine: String) = Unit

    override fun setUserId(userId: String) = Unit

    override fun setCustomKey(key: String, value: String) = Unit
}

object AppTelemetryProvider {
    /**
     * Returns the Firebase-backed telemetry when Firebase is ready, otherwise [NoOpAppTelemetry].
     * Any failure while checking or creating it also falls back to the no-op.
     */
    fun create(
        isFirebaseReady: () -> Boolean,
        createFirebaseTelemetry: () -> AppTelemetry,
    ): AppTelemetry {
        return runCatching {
            if (isFirebaseReady()) createFirebaseTelemetry() else NoOpAppTelemetry
        }.getOrDefault(NoOpAppTelemetry)
    }
}
