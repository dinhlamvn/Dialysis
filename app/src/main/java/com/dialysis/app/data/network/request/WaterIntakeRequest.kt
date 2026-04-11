package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class WaterIntakeRequest(
    @SerializedName("drink_name")
    val drinkName: String,
    @SerializedName("raw_amount")
    val rawAmount: Int,
    @SerializedName("weight_ratio")
    val weightRatio: Double,
    @SerializedName("logged_at")
    val loggedAt: String
)

