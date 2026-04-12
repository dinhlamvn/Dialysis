package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class WeightInitialRequest(
    @SerializedName("weight")
    val weight: Int
)
