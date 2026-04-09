package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class NetworkErrorResponse(
    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("errors")
    val errors: Map<String, List<String>>?
)
