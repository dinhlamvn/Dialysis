package com.dialysis.app.sharepref

import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AccountSharePref(context: Context) : SharePref(context, "account_pref") {

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_LAST_WATER_SYNC_AT = "last_water_sync_at"
    }

    fun setToken(token: String) {
        put(KEY_TOKEN, token)
    }

    fun getToken(): String {
        return get(KEY_TOKEN, "")
    }

    fun setTokenType(tokenType: String) {
        put(KEY_TOKEN_TYPE, tokenType)
    }

    fun getTokenType(): String {
        return get(KEY_TOKEN_TYPE, "")
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

    fun clear() {
        remove(KEY_TOKEN)
        remove(KEY_TOKEN_TYPE)
        remove(KEY_LAST_WATER_SYNC_AT)
    }
}
