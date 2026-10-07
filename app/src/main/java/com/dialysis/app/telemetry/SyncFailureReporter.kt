package com.dialysis.app.telemetry

enum class SyncDataType(val value: String) {
    WATER("water"),
    URINE("urine"),
}

enum class SyncOperation(val value: String) {
    CREATE("create"),
    DELETE("delete"),
    PULL("pull"),
}

/** Record data sent with a failure so it can be restored by hand. No other user data is sent. */
sealed interface RecordPayload {
    fun toParams(): Map<String, Any>

    data class Water(
        val amountMl: Int,
        val drinkName: String,
        val loggedAt: String,
    ) : RecordPayload {
        override fun toParams(): Map<String, Any> = mapOf(
            TelemetryParams.AMOUNT_ML to amountMl,
            TelemetryParams.DRINK_NAME to drinkName,
            TelemetryParams.LOGGED_AT to loggedAt,
        )
    }

    data class Urine(
        val amountMl: Int,
        val note: String?,
        val loggedAt: String,
        val clientId: String,
    ) : RecordPayload {
        override fun toParams(): Map<String, Any> = mapOf(
            TelemetryParams.AMOUNT_ML to amountMl,
            TelemetryParams.NOTE to note.orEmpty(),
            TelemetryParams.LOGGED_AT to loggedAt,
            TelemetryParams.CLIENT_ID to clientId,
        )
    }
}

/** Endpoint a sync operation calls, as declared in `AppServices` (path template, not the real id). */
data class ApiEndpoint(val method: String, val path: String) {
    companion object {
        val WATER_CREATE = ApiEndpoint("POST", "/mobile/water/intake")
        val WATER_DELETE = ApiEndpoint("DELETE", "/mobile/water/intake/{syncedId}")
        val WATER_HISTORY = ApiEndpoint("GET", "/mobile/water/history")
        val URINE_CREATE = ApiEndpoint("POST", "/mobile/urine/log")
    }
}

data class SyncFailure(
    val dataType: SyncDataType,
    val operation: SyncOperation,
    val endpoint: ApiEndpoint,
    val error: Throwable,
    val attempt: Int? = null,
    val recordId: String? = null,
    val recordCreatedAt: String? = null,
    val pendingCount: Int? = null,
    val payload: RecordPayload? = null,
)

object TelemetryEvents {
    const val SYNC_FAILED = "sync_failed"
    const val SYNC_COMPLETED = "sync_completed"
    const val WATER_INTAKE_SAVE_FAILED = "water_intake_save_failed"
    const val API_FAILED = "api_failed"
}

object TelemetryParams {
    const val DATA_TYPE = "data_type"
    const val OPERATION = "operation"
    const val ERROR_TYPE = "error_type"
    const val HTTP_CODE = "http_code"
    const val ERROR_MESSAGE = "error_message"
    const val ATTEMPT = "attempt"
    const val RECORD_ID = "record_id"
    const val RECORD_CREATED_AT = "record_created_at"
    const val PENDING_COUNT = "pending_count"
    const val AMOUNT_ML = "amount_ml"
    const val DRINK_NAME = "drink_name"
    const val LOGGED_AT = "logged_at"
    const val NOTE = "note"
    const val CLIENT_ID = "client_id"
    const val SUCCESS_COUNT = "success_count"
    const val FAILED_COUNT = "failed_count"
    const val API_METHOD = "api_method"
    const val API_PATH = "api_path"
    const val API_QUERY = "api_query"
    const val ERROR_BODY = "error_body"
    const val REQUEST_BODY = "request_body"
    const val USER_ID = "user_id"

    /** Per-failure Crashlytics keys. Ones a failure does not set are blanked so old values do not leak in. */
    val FAILURE_KEYS = listOf(
        DATA_TYPE, OPERATION, ERROR_TYPE, HTTP_CODE, ERROR_MESSAGE, ATTEMPT, RECORD_ID,
        RECORD_CREATED_AT, PENDING_COUNT, AMOUNT_ML, DRINK_NAME, LOGGED_AT, NOTE, CLIENT_ID,
        API_METHOD, API_PATH, API_QUERY, ERROR_BODY, REQUEST_BODY, USER_ID,
    )
}

/** Builds the failure / summary events. The only code that reports sync, save and API failures. */
class SyncFailureReporter(
    private val telemetry: AppTelemetry,
    /** Server user id of the logged-in account (null when logged out), sent with every failure. */
    private val userId: () -> String? = { null },
) {

    fun reportSyncFailure(failure: SyncFailure) {
        val params = buildMap {
            put(TelemetryParams.DATA_TYPE, failure.dataType.value)
            put(TelemetryParams.OPERATION, failure.operation.value)
            put(TelemetryParams.API_METHOD, failure.endpoint.method)
            put(TelemetryParams.API_PATH, failure.endpoint.path)
            putAll(errorParams(failure.error))
            failure.attempt?.let { put(TelemetryParams.ATTEMPT, it) }
            failure.recordId?.let { put(TelemetryParams.RECORD_ID, it) }
            failure.recordCreatedAt?.let { put(TelemetryParams.RECORD_CREATED_AT, it) }
            failure.pendingCount?.let { put(TelemetryParams.PENDING_COUNT, it) }
            failure.payload?.let { putAll(it.toParams()) }
        }
        val endpoint = "${failure.endpoint.method} ${failure.endpoint.path}"
        report(TelemetryEvents.SYNC_FAILED, failure.error, params, endpoint)
    }

    fun reportWaterSaveFailure(error: Throwable, payload: RecordPayload.Water) {
        val params = buildMap {
            put(TelemetryParams.DATA_TYPE, SyncDataType.WATER.value)
            putAll(errorParams(error))
            putAll(payload.toParams())
        }
        report(TelemetryEvents.WATER_INTAKE_SAVE_FAILED, error, params, endpoint = "local Room insert")
    }

    /**
     * One failed HTTP call, from `ApiFailureInterceptor`. [path] and [query] are the real request URL parts;
     * [requestBody] is the JSON sent (already redacted by the caller) so the data can be re-entered by hand;
     * [errorBody] is the server's error response. Headers (auth token) are never sent.
     */
    fun reportApiFailure(
        method: String,
        path: String,
        query: String?,
        error: Throwable,
        requestBody: String?,
        errorBody: String?,
    ) {
        val params = buildMap {
            put(TelemetryParams.API_METHOD, method)
            put(TelemetryParams.API_PATH, path)
            query?.let { put(TelemetryParams.API_QUERY, it) }
            putAll(errorParams(error))
            requestBody?.takeIf { it.isNotBlank() }?.let { put(TelemetryParams.REQUEST_BODY, it) }
            errorBody?.takeIf { it.isNotBlank() }?.let { put(TelemetryParams.ERROR_BODY, it) }
        }
        report(TelemetryEvents.API_FAILED, error, params, "$method ${path.replace(ID_SEGMENT, "/{id}")}")
    }

    fun reportSyncCompleted(dataType: SyncDataType, successCount: Int, failedCount: Int, attempt: Int) {
        telemetry.logEvent(
            TelemetryEvents.SYNC_COMPLETED,
            mapOf(
                TelemetryParams.DATA_TYPE to dataType.value,
                TelemetryParams.SUCCESS_COUNT to successCount,
                TelemetryParams.FAILED_COUNT to failedCount,
                TelemetryParams.ATTEMPT to attempt,
            )
        )
    }

    /**
     * Analytics gets values cut to 100 chars; Crashlytics keys and the log line keep up to 1024.
     * The non-fatal is a [TelemetryFailureException] named after [event] and [endpoint] (one Crashlytics
     * issue per endpoint) whose message shows the user, with the original error as its cause.
     */
    private fun report(event: String, error: Throwable, eventParams: Map<String, Any>, endpoint: String) {
        val params = buildMap {
            putAll(eventParams)
            userId()?.let { put(TelemetryParams.USER_ID, it) }
        }
        telemetry.logEvent(event, params.truncated(MAX_VALUE_LENGTH))
        val fullParams = params.truncated(MAX_CRASHLYTICS_VALUE_LENGTH)
        val customKeys = TelemetryParams.FAILURE_KEYS.associateWith { key -> fullParams[key] ?: "" }
        val logLine = event + fullParams.entries.joinToString(prefix = " ", separator = " ") { (key, value) ->
            "$key=$value"
        }
        val user = params[TelemetryParams.USER_ID] ?: "unknown"
        val nonFatal = TelemetryFailureException(
            event = event,
            endpoint = endpoint,
            message = "$event: $endpoint · user $user · ${params[TelemetryParams.ERROR_TYPE]}",
            cause = error,
        )
        telemetry.recordNonFatal(nonFatal, customKeys, logLine)
    }

    private fun errorParams(error: Throwable): Map<String, Any> {
        val info = ErrorClassifier.classify(error)
        return buildMap {
            put(TelemetryParams.ERROR_TYPE, info.type.value)
            info.httpCode?.let { put(TelemetryParams.HTTP_CODE, it) }
            put(TelemetryParams.ERROR_MESSAGE, error.message ?: error.javaClass.simpleName)
        }
    }

    private fun Map<String, Any>.truncated(maxLength: Int): Map<String, Any> =
        mapValues { (_, value) -> if (value is String) value.take(maxLength) else value }

    companion object {
        /** Analytics drops string values longer than 100 characters. */
        const val MAX_VALUE_LENGTH = 100

        /** Crashlytics custom key values are limited to 1024 characters. */
        const val MAX_CRASHLYTICS_VALUE_LENGTH = 1024

        /** Numeric path segments (record ids), folded so one endpoint is one Crashlytics issue. */
        private val ID_SEGMENT = Regex("/\\d+(?=/|$)")
    }
}

/**
 * Non-fatal sent to Crashlytics for a failure event. Its only stack frame is `<event>.<endpoint>`, so
 * Crashlytics groups one issue per event + endpoint and the issue list reads e.g.
 * `api_failed.POST /mobile/water/intake`. The original error (with its real stack trace) is the cause.
 */
class TelemetryFailureException(
    event: String,
    endpoint: String,
    message: String,
    cause: Throwable,
) : Exception(message, cause) {
    init {
        stackTrace = arrayOf(StackTraceElement(event, endpoint, null, -1))
    }
}
