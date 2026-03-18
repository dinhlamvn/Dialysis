package com.dialysis.app.sharepref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

abstract class SharePref(
    context: Context,
    sharePrefName: String
) {

    private val sharePref: SharedPreferences =
        context.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)

    fun put(key: String, value: Any, sync: Boolean = false) {
        if (key.isBlank()) {
            return
        }
        sharePref.edit(sync) {
            when (value) {
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                else -> error("Not support put for value: $value")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String, default: T): T {
        return sharePref.all[key] as? T ?: default
    }

    fun remove(key: String, sync: Boolean = false) {
        sharePref.edit(sync) {
            remove(key)
        }
    }
}
