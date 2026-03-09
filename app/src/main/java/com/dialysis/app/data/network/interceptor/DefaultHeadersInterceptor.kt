package com.dialysis.app.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

object DefaultHeadersInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("Accept", "application/json")
        return chain.proceed(request.build())
    }
}