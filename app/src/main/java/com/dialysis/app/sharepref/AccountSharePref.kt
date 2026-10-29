package com.dialysis.app.sharepref

import android.content.Context
import com.dialysis.app.data.network.auth.SessionStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AccountSharePref(context: Context) : SharePref(context, "account_pref"), SessionStore {

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_LAST_WATER_SYNC_AT = "last_water_sync_at"
        private const val KEY_HAS_LOGGED_IN = "has_logged_in"
        private const val KEY_LOGIN_IDENTIFIER = "login_identifier"
    }

    override fun setToken(token: String) {
        put(KEY_TOKEN, token)
    }

    override fun getToken(): String {
        return get(KEY_TOKEN, "")
    }

    override fun setTokenType(tokenType: String) {
        put(KEY_TOKEN_TYPE, tokenType)
    }

    fun getTokenType(): String {
        return get(KEY_TOKEN_TYPE, "")
    }

    /** Username of the last successful login, used for the silent re-login on 401. */
    fun setLoginIdentifier(identifier: String) {
        put(KEY_LOGIN_IDENTIFIER, identifier)
    }

    override fun getLoginIdentifier(): String {
        return get(KEY_LOGIN_IDENTIFIER, "")
    }

    fun setLastWaterSyncAt(timestampMillis: Long) {
        put(KEY_LAST_WATER_SYNC_AT, timestampMillis)
    }

    fun getLastWaterSyncAt(): Long? {
        return get(KEY_LAST_WATER_SYNC_AT, 0L).takeIf { it > 0L }
    }

    fun observeLastWaterSyncAt(): Flow<Long?> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_LAST_WATER_SYNC_AT) {
                trySend(getLastWaterSyncAt())
            }
        }

        trySend(getLastWaterSyncAt())
        sharePref.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharePref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /**
     * Session expired: drops only the token, so the user must log in again. Local water/urine/weight
     * data and pending records stay and are pushed by the sync after the next login.
     */
    fun clearSession() {
        remove(KEY_TOKEN)
        remove(KEY_TOKEN_TYPE)
    }

    fun clear() {
        remove(KEY_TOKEN)
        remove(KEY_TOKEN_TYPE)
        remove(KEY_LAST_WATER_SYNC_AT)
        remove(KEY_HAS_LOGGED_IN)
        remove(KEY_LOGIN_IDENTIFIER)
    }

    fun setHasLoggedIn(value: Boolean) {
        put(KEY_HAS_LOGGED_IN, value)
    }

    fun hasLoggedIn(): Boolean {
        return get(KEY_HAS_LOGGED_IN, false)
    }
}
