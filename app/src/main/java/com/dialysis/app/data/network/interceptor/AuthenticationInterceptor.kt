package com.dialysis.app.data.network.interceptor

import com.dialysis.app.sharepref.AccountSharePref
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor(private val accountSharePref: AccountSharePref) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer ${accountSharePref.getToken()}")
        return chain.proceed(request.build())
    }
}