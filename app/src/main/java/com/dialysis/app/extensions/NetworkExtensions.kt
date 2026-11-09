package com.dialysis.app.extensions

import com.dialysis.app.data.network.response.NetworkErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

fun Throwable.parseNetworkErrorResponse(appGson: Gson): NetworkErrorResponse {
    val httpException = this.castToAsNull<HttpException>() ?: return defaultNetworkErrorResponse()
    // Reading the body can fail on a dropped connection; callers must still get a failure Result.
    val body = runCatching { httpException.response()?.errorBody()?.string() }.getOrNull()
        ?: return defaultNetworkErrorResponse()
    return appGson.runCatching {
        val networkErrorResponse = fromJson(body, NetworkErrorResponse::class.java)
        val errors = networkErrorResponse.errors.orEmpty()
        if (errors.isNotEmpty()) {
            val originalMessage = networkErrorResponse.message
            val key = errors.keys.first()
            val arrayMessages = errors.getOrDefault(key, emptyList())
            networkErrorResponse.copy(message = arrayMessages.firstOrNull() ?: originalMessage)
        } else {
            networkErrorResponse
        }
    }.getOrDefault(defaultNetworkErrorResponse())
}

private fun Throwable.defaultNetworkErrorResponse(): NetworkErrorResponse {
    return NetworkErrorResponse(
        "Code: 0",
        this.message ?: "Lỗi không xác định",
        emptyMap()
    )
}
