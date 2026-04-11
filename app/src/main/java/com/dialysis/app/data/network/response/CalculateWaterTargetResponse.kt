package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class CalculateWaterTargetResponse(
    @SerializedName("daily_water_target")
    val dailyWaterTarget: Int?,
    @SerializedName("daily_urine_ml")
    val dailyUrineMl: Int?,
    @SerializedName("weight")
    val weight: Double?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("height")
    val height: Double?,
    @SerializedName("activity_level")
    val activityLevel: String?,
    @SerializedName("age")
    val age: Int?,
)
