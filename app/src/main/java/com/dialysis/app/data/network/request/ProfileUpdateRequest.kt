package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("gender") val gender: String,
    @SerializedName("name") val name: String,
    @SerializedName("dialysis_start_year") val dialysisStartYear: Int,
    @SerializedName("daily_water_target") val dailyWaterTarget: Int,
    @SerializedName("age") val age: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("dialysis_freq_week") val dialysisFreqWeek: Int,
    @SerializedName("daily_urine_ml") val dailyUrineMl: Int,
    @SerializedName("initial_weight") val initialWeight: Int,
)