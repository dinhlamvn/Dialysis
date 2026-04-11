package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class WaterIntakeResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("drink_name")
    val drinkName: String,
    @SerializedName("raw_amount")
    val rawAmount: Int,
    @SerializedName("weight_ratio")
    val weightRatio: Double,
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("logged_at")
    val loggedAt: String,
    @SerializedName("created_at")
    val createdAt: String
)

