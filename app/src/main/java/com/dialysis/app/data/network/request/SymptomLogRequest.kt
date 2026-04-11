package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class SymptomLogRequest(
    @SerializedName("symptom")
    val symptom: String,
    @SerializedName("notes")
    val notes: String,
    @SerializedName("logged_at")
    val loggedAt: String
)

