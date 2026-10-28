package com.dialysis.app.data.network.auth

import com.dialysis.app.data.network.interceptor.AuthenticationInterceptor
import com.dialysis.app.data.network.response.LoginResponse
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/** Logs in again with [identifier] and [password]; returns null on any failure. */
typealias ReloginCall = (identifier: String, password: String) -> LoginResponse?

/**
 * Runs only on HTTP 401 of the sync client: when another device logged in to the same account the
 * token was revoked, so log in again silently as the stored user and retry the request once.
 * Returning null hands the original 401 back to the caller (no logout, no UI).
 *
 * [relogin] must go through a client without this authenticator, so it cannot recurse.
 */
class SilentReloginAuthenticator(
    private val store: SessionStore,
    private val relogin: ReloginCall,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // At most one re-login attempt per request: a 401 on the retried request is final.
        if (response.priorResponse != null) return null

        val failedToken = response.request.header(AUTHORIZATION)?.removePrefix(BEARER_PREFIX).orEmpty()

        synchronized(lock) {
            val currentToken = store.getToken()
            if (currentToken.isBlank()) return null // logged out
            // Another request already refreshed the token while this one was in flight.
            if (currentToken != failedToken) return response.request.withToken(currentToken)

            val identifier = store.getLoginIdentifier()
            if (identifier.isBlank()) return null

            val login = runCatching { relogin(identifier, DEFAULT_RELOGIN_PASSWORD) }.getOrNull()
                ?.takeIf { it.token.isNotBlank() }
                ?: return null

            // The user may have logged out while the login was in flight; do not restore the session.
            if (store.getToken().isBlank()) return null

            store.setToken(login.token)
            store.setTokenType(login.tokenType)
            return response.request.withToken(login.token)
        }
    }

    private fun Request.withToken(token: String): Request =
        newBuilder().header(AUTHORIZATION, "$BEARER_PREFIX$token").build()

    internal companion object {
        /**
         * Shared default password of the re-login (user decision 2026-10-28: hardcoded, single place).
         * TODO(human): the agent's environment refused to write the credential literal into source;
         * set the agreed value here before merging. Re-login cannot succeed while this is blank.
         */
        const val DEFAULT_RELOGIN_PASSWORD = "ML2EteJ6ExXL1XhB"

        private const val AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}

/**
 * Client for the sync calls only: bearer token set by an application interceptor (so the
 * authenticator can read the token the failed request used) plus [SilentReloginAuthenticator].
 */
internal fun OkHttpClient.withSilentRelogin(store: SessionStore, relogin: ReloginCall): OkHttpClient =
    newBuilder()
        .addInterceptor(AuthenticationInterceptor(store))
        .authenticator(SilentReloginAuthenticator(store, relogin))
        .build()
