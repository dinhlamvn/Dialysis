package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class UrineLogResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("logged_at")
    val loggedAt: String?,
    @SerializedName("note")
    val note: String?,
    @SerializedName("client_id")
    val clientId: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class UrineHistoryItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("from_date")
    val fromDate: String?,
    @SerializedName("to_date")
    val toDate: String?,
    @SerializedName("value")
    val value: Int,
    @SerializedName("note")
    val note: String?,
    @SerializedName("client_id")
    val clientId: String?,
    @SerializedName("created_at")
    val createdAt: String?
)
