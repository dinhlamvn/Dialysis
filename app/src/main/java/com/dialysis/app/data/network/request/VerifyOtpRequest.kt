package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("identifier_type")
    val identifierType: String,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("otp_code")
    val otpCode: String,
)
