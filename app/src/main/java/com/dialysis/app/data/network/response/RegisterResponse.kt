package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("identifier_type")
    val identifierType: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
)
