package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class WeightInitialRequest(
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("date")
    val date: String,
    @SerializedName("note")
    val note: String
)
