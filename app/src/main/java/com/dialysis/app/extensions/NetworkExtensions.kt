package com.dialysis.app.extensions

import com.dialysis.app.data.network.response.NetworkErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

fun Throwable.parseNetworkErrorResponse(): NetworkErrorResponse {
    val httpException = this.castToAsNull<HttpException>() ?: return defaultNetworkErrorResponse()
    val body =
        httpException.response()?.errorBody()?.string() ?: return defaultNetworkErrorResponse()
    return Gson().runCatching {
        fromJson(body, NetworkErrorResponse::class.java)
    }.getOrDefault(defaultNetworkErrorResponse())
}

private fun Throwable.defaultNetworkErrorResponse(): NetworkErrorResponse {
    return NetworkErrorResponse(
        "Code: 0",
        this.message ?: "Unknown Error"
    )
}