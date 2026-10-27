package com.dialysis.app.data.network.request

import com.google.gson.annotations.SerializedName

/** Body of `POST mobile/sync`: offline records pushed in one batch. */
data class MobileSyncRequest(
    @SerializedName("items")
    val items: List<MobileSyncItem>
)

/**
 * One offline record. [type] is e.g. [TYPE_WATER_INTAKE]; [data] is the type's own payload
 * (for water: [WaterIntakeRequest]), so weight/symptom items can reuse this model later.
 */
data class MobileSyncItem(
    @SerializedName("type")
    val type: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("data")
    val data: Any,
    @SerializedName("created_at_local")
    val createdAtLocal: String
) {
    companion object {
        const val TYPE_WATER_INTAKE = "water_intake"
    }
}
