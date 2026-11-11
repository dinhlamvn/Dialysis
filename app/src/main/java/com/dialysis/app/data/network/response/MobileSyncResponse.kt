package com.dialysis.app.data.network.response

import com.google.gson.annotations.SerializedName

/** `data` of the `POST mobile/sync` response. */
data class MobileSyncResponseData(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("success_count")
    val successCount: Int = 0,
    @SerializedName("fail_count")
    val failCount: Int = 0,
    @SerializedName("results")
    val results: List<MobileSyncResult>? = null
)

/** Per-item result, matched to the local record by [clientId]. */
data class MobileSyncResult(
    @SerializedName("index")
    val index: Int? = null,
    @SerializedName("client_id")
    val clientId: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("server_id")
    val serverId: Long? = null
)
