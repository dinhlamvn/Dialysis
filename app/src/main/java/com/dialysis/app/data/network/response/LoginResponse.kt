package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("user")
    val user: LoginUser,
    @SerializedName("token")
    val token: String,
    @SerializedName("token_type")
    val tokenType: String,
)

data class LoginUser(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("weight")
    val weight: Double?,
    @SerializedName("initial_weight")
    val initialWeight: Double?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("age")
    val age: Int?,
    @SerializedName("dialysis_start_year")
    val dialysisStartYear: Int?,
    @SerializedName("dialysis_freq_week")
    val dialysisFreqWeek: Int?,
    @SerializedName("daily_urine_ml")
    val dailyUrineMl: Int?,
    @SerializedName("daily_water_target")
    val dailyWaterTarget: Int?,
    @SerializedName("reminder_start_time")
    val reminderStartTime: String?,
    @SerializedName("reminder_end_time")
    val reminderEndTime: String?,
    @SerializedName("reminder_interval")
    val reminderInterval: Int?,
    @SerializedName("reminder_enabled")
    val reminderEnabled: Boolean?,
    @SerializedName("email_verified")
    val emailVerified: Boolean,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,
    @SerializedName("phone_verified")
    val phoneVerified: Boolean,
    @SerializedName("phone_verified_at")
    val phoneVerifiedAt: String?,
    @SerializedName("has_google")
    val hasGoogle: Boolean,
    @SerializedName("has_facebook")
    val hasFacebook: Boolean,
    @SerializedName("has_apple")
    val hasApple: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
)
