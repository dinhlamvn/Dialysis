package com.dialysis.app.sharepref

import android.content.Context

class AccountSharePref(context: Context) : SharePref(context, "account_pref") {

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_TOKEN_TYPE = "token_type"
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
}