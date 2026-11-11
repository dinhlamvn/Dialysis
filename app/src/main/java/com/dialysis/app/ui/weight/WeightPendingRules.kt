package com.dialysis.app.ui.weight

import com.dialysis.app.data.local.entity.WeightEntryEntity

/** Latest local weight, falling back to the initial weight; 0 when neither is known. */
internal fun displayedCurrentWeight(latestLocalKg: Float?, initialWeightKg: Float): Float {
    val weight = latestLocalKg ?: initialWeightKg
    return if (weight > 0f) weight else 0f
}

/** The server does not know a pending latest entry yet, so its current weight must not replace it. */
internal fun currentWeightAfterServerRefresh(serverKg: Float, latestLocal: WeightEntryEntity?): Float =
    if (latestLocal != null && latestLocal.serverId == null) latestLocal.weightKg else serverKg
