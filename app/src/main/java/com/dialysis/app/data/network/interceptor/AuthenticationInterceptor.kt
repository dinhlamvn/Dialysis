package com.dialysis.app.data.network.interceptor

import com.dialysis.app.data.network.auth.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor(private val sessionStore: SessionStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer ${sessionStore.getToken()}")
        return chain.proceed(request.build())
    }
}