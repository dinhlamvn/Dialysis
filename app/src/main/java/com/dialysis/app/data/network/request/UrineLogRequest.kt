package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class UrineLogRequest(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("logged_at")
    val loggedAt: String,
    @SerializedName("note")
    val note: String?,
    @SerializedName("client_id")
    val clientId: String
)
