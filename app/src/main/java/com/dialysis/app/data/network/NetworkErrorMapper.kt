package com.dialysis.app.data.network

import com.dialysis.app.extensions.parseNetworkErrorResponse
import com.google.gson.Gson
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.Collections
import java.util.IdentityHashMap

/**
 * The device has no usable connection. Screens show the shared no-internet dialog for it,
 * never this message; the message is only a readable fallback.
 */
class NoInternetException(cause: Throwable? = null) :
    Exception("Vui lòng kết nối mạng để sử dụng chức năng", cause)

/** True when this error, or any of its causes, means the request never reached the server. */
fun Throwable.isNoInternet(): Boolean {
    val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
    var current: Throwable? = this
    while (current != null && seen.add(current)) {
        if (current.isNoInternetError()) return true
        current = current.cause
    }
    return false
}

/** The one mapping used by [NetworkManager.resolve] and [NetworkManager.resolveNullable]. */
internal fun Throwable.toResolveFailure(appGson: Gson): Exception =
    if (isNoInternet()) {
        NoInternetException(this)
    } else {
        Exception(parseNetworkErrorResponse(appGson).message)
    }

private fun Throwable.isNoInternetError(): Boolean = when (this) {
    is NoInternetException,
    is UnknownHostException,
    is ConnectException,
    is NoRouteToHostException -> true
    is SocketException -> message.isDroppedConnectionMessage()
    else -> false
}

private val DROPPED_CONNECTION_MARKERS = listOf(
    "connection reset",
    "socket closed",
    "socket is closed",
    "connection closed",
    "broken pipe",
)

private fun String?.isDroppedConnectionMessage(): Boolean {
    val normalized = this?.lowercase() ?: return false
    return DROPPED_CONNECTION_MARKERS.any(normalized::contains)
}

/** True when this result failed because the device has no connection. */
fun Result<*>.isNoInternetFailure(): Boolean = exceptionOrNull()?.isNoInternet() == true
