package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class NetworkErrorResponse(
    @SerializedName("messageCode")
    val messageCode: String,

    @SerializedName("message")
    val message: String,
)
