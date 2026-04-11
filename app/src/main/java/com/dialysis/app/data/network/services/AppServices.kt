package com.dialysis.app.data.network.services

import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.request.SymptomLogRequest
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.LoginUser
import com.dialysis.app.data.network.response.WaterIntakeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppServices {

    @GET("mobile/auth/me")
    suspend fun me(): ApiResponse<LoginUser>

    @POST("mobile/water/intake")
    suspend fun syncWaterIntake(
        @Body request: WaterIntakeRequest
    ): ApiResponse<WaterIntakeResponse>

    @DELETE("mobile/water/intake/{syncedId}")
    suspend fun deleteWaterIntake(
        @Path("syncedId") syncedId: Long
    ): ApiResponse<Map<String, Any>?>

    @GET("mobile/symptoms/list")
    suspend fun getSymptoms(): ApiResponse<List<String>>

    @POST("mobile/symptoms/log")
    suspend fun logSymptom(
        @Body request: SymptomLogRequest
    ): ApiResponse<Map<String, Any>?>
}
