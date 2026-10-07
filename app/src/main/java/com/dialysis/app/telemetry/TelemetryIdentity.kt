package com.dialysis.app.telemetry

import java.net.URI

/**
 * Sets the Crashlytics user id and the app-wide custom keys. The user id is the server user id,
 * or an anonymous install id when nobody is logged in. No other account data is sent.
 */
class TelemetryIdentity(
    private val telemetry: AppTelemetry,
    private val serverUserId: () -> String?,
    private val installId: () -> String,
) {

    fun setAppKeys(flavor: String, serverUrl: String) {
        telemetry.setCustomKey(KEY_APP_FLAVOR, flavor)
        telemetry.setCustomKey(KEY_SERVER_URL_HOST, hostOf(serverUrl))
    }

    /** Call at startup and whenever the account changes (login, sign-out). */
    fun refreshUserId() {
        telemetry.setUserId(serverUserId() ?: installId())
    }

    private fun hostOf(url: String): String = runCatching { URI(url).host }.getOrNull().orEmpty()

    companion object {
        const val KEY_APP_FLAVOR = "app_flavor"
        const val KEY_SERVER_URL_HOST = "server_url_host"
    }
}
