package com.dialysis.app.data.network.services

import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.request.SymptomLogRequest
import com.dialysis.app.data.network.request.UrineLogRequest
import com.dialysis.app.data.network.request.WeightInitialRequest
import com.dialysis.app.data.network.request.WeightLogRequest
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.LoginUser
import com.dialysis.app.data.network.response.UrineHistoryItem
import com.dialysis.app.data.network.response.UrineLogResponse
import com.dialysis.app.data.network.response.WaterIntakeResponse
import com.dialysis.app.data.network.response.WeightActionResponse
import com.dialysis.app.data.network.response.WeightChartResponse
import com.dialysis.app.data.network.response.WeightCurrentResponse
import com.dialysis.app.data.network.response.WeightHistoryItem
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

    @DELETE("mobile/auth/account")
    suspend fun deleteAccount(): ApiResponse<Map<String, Any>?>

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

    @POST("mobile/urine/log")
    suspend fun logUrine(
        @Body request: UrineLogRequest
    ): ApiResponse<UrineLogResponse>

    @GET("mobile/urine/history")
    suspend fun getUrineHistory(): ApiResponse<List<UrineHistoryItem>>

    @PUT("mobile/weight/initial")
    suspend fun updateInitialWeight(
        @Body request: WeightInitialRequest
    ): ApiResponse<WeightActionResponse?>

    @POST("mobile/weight/log")
    suspend fun logCurrentWeight(
        @Body request: WeightLogRequest
    ): ApiResponse<WeightActionResponse?>

    @GET("mobile/weight/current")
    suspend fun getCurrentWeight(): ApiResponse<WeightCurrentResponse?>

    @GET("mobile/weight/history")
    suspend fun getWeightHistory(
        @Query("limit") limit: Int
    ): ApiResponse<List<WeightHistoryItem>>

    @GET("mobile/weight/chart")
    suspend fun getWeightChart(
        @Query("period") period: String,
        @Query("month") month: Int?,
        @Query("year") year: Int?
    ): ApiResponse<WeightChartResponse?>

    @DELETE("mobile/weight/{id}")
    suspend fun deleteWeight(
        @Path("id") id: Long
    ): ApiResponse<WeightActionResponse?>
}
