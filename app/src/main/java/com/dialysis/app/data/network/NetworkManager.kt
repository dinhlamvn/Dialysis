package com.dialysis.app.data.network

import com.dialysis.app.BuildConfig
import com.dialysis.app.data.network.interceptor.DefaultHeadersInterceptor
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.services.AppServices
import com.dialysis.app.data.network.services.AppPublicServices
import com.dialysis.app.extensions.parseNetworkErrorResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkManager {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(DefaultHeadersInterceptor)
        .build()

    private val appGson: Gson = GsonBuilder()
        .create()

    private val retrofitBuilder =
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(appGson))

    val appPublicServices: AppPublicServices by lazy {
        retrofitBuilder.baseUrl(BuildConfig.SERVER_URL)
            .buildWithClient(okHttpClient)
            .create(AppPublicServices::class.java)
    }

    val appServices: AppServices by lazy {
        retrofitBuilder.baseUrl(BuildConfig.SERVER_URL)
            .buildWithClient(okHttpClient)
            .create(AppServices::class.java)
    }

    suspend fun <T> resolve(block: suspend () -> ApiResponse<T>): Result<T> {
        return try {
            val response = block()
            if (response.success) {
                Result.success(response.data!!)
            } else {
                Result.failure(UnknownError())
            }
        } catch (e: Exception) {
            val networkError = e.parseNetworkErrorResponse()
            Result.failure(Exception(networkError.message))
        }
    }

    private fun Retrofit.Builder.buildWithClient(
        okHttpClient: OkHttpClient,
        shouldLog: Boolean = true
    ): Retrofit {
        return client(okHttpClient.newBuilder().apply {
            if (BuildConfig.DEBUG && shouldLog) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            readTimeout(if (BuildConfig.DEBUG) 5 else 1, TimeUnit.MINUTES)
            writeTimeout(if (BuildConfig.DEBUG) 5 else 1, TimeUnit.MINUTES)
            connectTimeout(if (BuildConfig.DEBUG) 5 else 1, TimeUnit.MINUTES)
        }.build()).build()
    }
}
