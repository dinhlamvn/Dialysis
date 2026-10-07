package com.dialysis.app.data.network.interceptor

import com.dialysis.app.data.network.ApiException
import com.dialysis.app.telemetry.SyncFailureReporter
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * Reports every failed API call as `api_failed`: transport errors (network, SSL, timeout), HTTP >= 400,
 * and JSON responses with `success = false`. Sends method, URL path/query, the JSON request body (so the
 * data can be re-entered by hand in the web admin) and the server's response. Headers (auth token) are
 * never sent; `/mobile/auth/` bodies are left out and secret-like fields are masked.
 * Reporting never changes the call's result.
 */
class ApiFailureInterceptor(private val reporter: SyncFailureReporter) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            if (!chain.call().isCanceled()) report(request, error = e, errorBody = null)
            throw e
        }
        runCatching { checkResponse(request, response) }
        return response
    }

    private fun checkResponse(request: Request, response: Response) {
        if (!response.isSuccessful) {
            val body = response.peekBody(MAX_PEEK_BYTES).string()
            val message = body.jsonObjectOrNull()?.stringOrNull("message") ?: "HTTP ${response.code}"
            report(request, ApiException(message, httpCode = response.code), body)
            return
        }
        if (response.body?.contentType()?.subtype != "json") return
        val json = response.peekBody(MAX_PEEK_BYTES).string().jsonObjectOrNull() ?: return
        val success = json.get("success")
        if (success != null && success.isJsonPrimitive && success.asJsonPrimitive.isBoolean && !success.asBoolean) {
            val message = json.stringOrNull("message") ?: "success=false"
            report(request, ApiException(message), errorBody = null)
        }
    }

    private fun report(request: Request, error: Throwable, errorBody: String?) {
        runCatching {
            reporter.reportApiFailure(
                method = request.method,
                path = request.url.encodedPath,
                query = request.url.encodedQuery,
                error = error,
                requestBody = request.redactedBody(),
                errorBody = errorBody,
            )
        }
    }

    private fun Request.redactedBody(): String? {
        if (url.encodedPath.startsWith(AUTH_PATH_PREFIX)) return null
        val body = body ?: return null
        if (body.contentType()?.subtype != "json" || body.isOneShot() || body.contentLength() > MAX_PEEK_BYTES) {
            return null
        }
        val text = Buffer().also { body.writeTo(it) }.readUtf8()
        val json = text.jsonObjectOrNull() ?: return text
        json.keySet().filter { key -> SECRET_KEYS.any { key.contains(it, ignoreCase = true) } }
            .forEach { key -> json.addProperty(key, "***") }
        return json.toString()
    }

    private fun String.jsonObjectOrNull(): JsonObject? =
        runCatching { JsonParser.parseString(this).asJsonObject }.getOrNull()

    private fun JsonObject.stringOrNull(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString

    private companion object {
        /** Error and `success = false` responses are small; larger bodies are cut and then skipped as unparsable. */
        const val MAX_PEEK_BYTES = 16L * 1024

        /** Login, register, OTP, password and profile calls carry credentials or personal data. */
        const val AUTH_PATH_PREFIX = "/mobile/auth/"
        val SECRET_KEYS = listOf("password", "otp", "token", "phone", "email")
    }
}
