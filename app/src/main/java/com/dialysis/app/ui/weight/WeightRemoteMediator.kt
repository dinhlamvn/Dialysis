package com.dialysis.app.ui.weight

import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.WeightInitialRequest
import com.dialysis.app.data.network.request.WeightLogRequest
import com.dialysis.app.sharepref.AccountSharePref

internal class WeightRemoteMediator(
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager
) {
    fun hasAuthToken(): Boolean = accountSharePref.getToken().isNotBlank()

    suspend fun fetchHistory(limit: Int): List<WeightEntryEntity>? {
        if (!hasAuthToken()) return null
        return networkManager.resolve { networkManager.appServices.getWeightHistory(limit = limit) }
            .getOrNull()
            ?.mapNotNull { it.toEntity() }
    }

    suspend fun fetchCurrentWeight(): Float? {
        if (!hasAuthToken()) return null
        return networkManager.resolveNullable { networkManager.appServices.getCurrentWeight() }
            .getOrNull()
            ?.weight
            ?.toFloat()
    }

    suspend fun syncInitialWeight(weightKg: Float): Boolean {
        if (!hasAuthToken()) return true
        return networkManager.resolveNullable {
            networkManager.appServices.updateInitialWeight(
                WeightInitialRequest(
                    weight = weightKg.toDouble(),
                    date = formatWeightApiDate(System.currentTimeMillis()),
                    note = ""
                )
            )
        }.isSuccess
    }

    suspend fun syncCurrentWeight(weightKg: Float): Long? {
        if (!hasAuthToken()) return null
        return networkManager.resolveNullable {
            networkManager.appServices.logCurrentWeight(
                WeightLogRequest(
                    weight = weightKg.toDouble(),
                    date = formatWeightApiDate(System.currentTimeMillis()),
                    note = DEFAULT_WEIGHT_NOTE
                )
            )
        }.getOrNull()?.id
    }

    suspend fun deleteWeight(serverId: Long) {
        if (!hasAuthToken()) return
        networkManager.resolveNullable { networkManager.appServices.deleteWeight(serverId) }
    }

    suspend fun fetchChart(
        tab: WeightReportTab,
        range: WeightReportRange,
        goalWeight: Float
    ): WeightChartResultWithStats? {
        if (!hasAuthToken()) return null
        val result = networkManager.resolveNullable {
            networkManager.appServices.getWeightChart(
                period = if (tab == WeightReportTab.MONTH) "month" else "year",
                month = if (tab == WeightReportTab.MONTH) range.monthNumber else null,
                year = range.year
            )
        }.getOrNull() ?: return null

        val points = result.data.orEmpty().toChartPoints(tab, range)
        val labels = buildWeightAxisLabels(tab, range)
        return WeightChartResultWithStats(
            chart = buildWeightChartFromPoints(points, labels, goalWeight, result.stats),
            stats = result.stats?.toUi()
        )
    }
}

internal data class WeightChartResultWithStats(
    val chart: WeightChartResult,
    val stats: WeightChartStatsUi?
)
