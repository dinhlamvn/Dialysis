package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("otp_code")
    val otpCode: String,
    @SerializedName("new_password")
    val newPassword: String
)
