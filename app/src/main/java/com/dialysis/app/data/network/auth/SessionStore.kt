package com.dialysis.app.data.network.auth

/** Session values the network layer reads and writes; kept Android-free so it can be faked in unit tests. */
interface SessionStore {
    fun getToken(): String
    fun setToken(token: String)
    fun setTokenType(tokenType: String)

    /** Identifier (username) of the last successful login, or blank if none / logged out. */
    fun getLoginIdentifier(): String
}
