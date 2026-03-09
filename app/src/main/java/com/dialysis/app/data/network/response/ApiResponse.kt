package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T?,
)