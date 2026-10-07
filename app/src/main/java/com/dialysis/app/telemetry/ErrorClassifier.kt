package com.dialysis.app.telemetry

import com.dialysis.app.data.network.ApiException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException

enum class ErrorType(val value: String) {
    NETWORK("network"),
    TIMEOUT("timeout"),
    HTTP("http"),
    PARSE("parse"),
    SERVER("server"),
    UNKNOWN("unknown"),
}

data class ErrorInfo(val type: ErrorType, val httpCode: Int?)

object ErrorClassifier {

    fun classify(error: Throwable): ErrorInfo {
        val httpCode = error.findHttpCode()
        val type = when {
            httpCode != null -> if (httpCode >= 500) ErrorType.SERVER else ErrorType.HTTP
            error is ApiException && error.cause == null -> ErrorType.SERVER
            else -> classifyCause(error)
        }
        return ErrorInfo(type, httpCode)
    }

    private fun classifyCause(error: Throwable): ErrorType {
        for (cause in error.causeChain()) {
            when (cause) {
                // Parse errors first: MalformedJsonException is an IOException.
                is JsonParseException, is MalformedJsonException -> return ErrorType.PARSE
                // SocketTimeoutException is an InterruptedIOException.
                is InterruptedIOException -> return ErrorType.TIMEOUT
                is IOException -> return ErrorType.NETWORK
            }
        }
        return ErrorType.UNKNOWN
    }

    private fun Throwable.findHttpCode(): Int? {
        for (cause in causeChain()) {
            when (cause) {
                is ApiException -> cause.httpCode?.let { return it }
                is HttpException -> return cause.code()
            }
        }
        return null
    }

    private fun Throwable.causeChain(): Sequence<Throwable> =
        generateSequence(this) { current -> current.cause?.takeIf { it !== current } }
            .take(MAX_CAUSE_DEPTH)

    private const val MAX_CAUSE_DEPTH = 10
}
