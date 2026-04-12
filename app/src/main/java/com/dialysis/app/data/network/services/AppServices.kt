package com.dialysis.app.data.network.services

import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.request.SymptomLogRequest
import com.dialysis.app.data.network.request.WeightInitialRequest
import com.dialysis.app.data.network.request.WeightLogRequest
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.LoginUser
import com.dialysis.app.data.network.response.WaterIntakeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AppServices {

    @GET("mobile/auth/me")
    suspend fun me(): ApiResponse<LoginUser>

    @POST("mobile/water/intake")
    suspend fun syncWaterIntake(
        @Body request: WaterIntakeRequest
    ): ApiResponse<WaterIntakeResponse>

    @GET("mobile/water/history")
    suspend fun getWaterHistory(
        @Query("page") page: Int
    ): ApiResponse<List<WaterIntakeResponse>>

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

    @PUT("mobile/weight/initial")
    suspend fun updateInitialWeight(
        @Body request: WeightInitialRequest
    ): ApiResponse<Map<String, Any>?>

    @POST("mobile/weight/log")
    suspend fun logCurrentWeight(
        @Body request: WeightLogRequest
    ): ApiResponse<Map<String, Any>?>
}
