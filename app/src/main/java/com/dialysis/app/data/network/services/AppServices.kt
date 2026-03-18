package com.dialysis.app.data.network.services

import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.LoginUser
import retrofit2.http.GET

interface AppServices {

    @GET("mobile/auth/me")
    suspend fun me(): ApiResponse<LoginUser>
}
