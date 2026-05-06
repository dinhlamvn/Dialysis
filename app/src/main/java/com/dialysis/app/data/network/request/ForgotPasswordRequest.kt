package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("identifier")
    val identifier: String
)
