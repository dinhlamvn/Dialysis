package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class CalculateWaterTargetRequest(
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("height")
    val height: Double,
    @SerializedName("activity_level")
    val activityLevel: String,
    @SerializedName("age")
    val age: Int,
)
