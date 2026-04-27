package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

data class WeightActionResponse(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("weight")
    val weight: Double?,
    @SerializedName("logged_date")
    val loggedDate: String?,
    @SerializedName("note")
    val note: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

data class WeightCurrentResponse(
    @SerializedName("weight")
    val weight: Double?
)

data class WeightHistoryItem(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("weight")
    val weight: Double?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("logged_date")
    val loggedDate: String?,
    @SerializedName("note")
    val note: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class WeightChartResponse(
    @SerializedName("period")
    val period: String?,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    @SerializedName("data")
    val data: List<WeightChartItem>?,
    @SerializedName("stats")
    val stats: WeightChartStats?
)

data class WeightChartItem(
    @SerializedName("label")
    val label: String?,
    @SerializedName("value")
    val value: Double?,
    @SerializedName("weight")
    val weight: Double?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("month")
    val month: Int?
) {
    val resolvedValue: Double?
        get() = value ?: weight
}

data class WeightChartStats(
    @SerializedName("min")
    val min: Double?,
    @SerializedName("max")
    val max: Double?,
    @SerializedName("avg")
    val avg: Double?,
    @SerializedName("count")
    val count: Int?
)
