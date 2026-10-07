package com.dialysis.app.data.network

import com.dialysis.app.extensions.parseNetworkErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

/**
 * Failure returned by [NetworkManager.resolve]. [message] is the text shown to the user (unchanged
 * from before); [httpCode] and [cause] keep the technical detail for telemetry.
 * `cause == null` means the server answered with `success = false`.
 */
class ApiException(
    message: String,
    val httpCode: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

/** Maps a thrown request error to an [ApiException], keeping the HTTP status and original cause. */
fun Throwable.toApiException(gson: Gson): ApiException {
    return ApiException(
        message = parseNetworkErrorResponse(gson).message,
        httpCode = (this as? HttpException)?.code(),
        cause = this,
    )
}
