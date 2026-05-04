package com.dialysis.app.ui.weight

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.config.AppGoals
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WeightViewModel(
    private val weightTrackingRepository: WeightTrackingRepository,
    private val userProfileSharePref: UserProfileSharePref,
    private val accountSharePref: AccountSharePref,
    private val networkManager: NetworkManager
) : BaseViewModel<WeightState>(WeightState()) {

    private val remoteMediator = WeightRemoteMediator(accountSharePref, networkManager)
    private val selectedTabFlow = MutableStateFlow(WeightReportTab.MONTH)
    private val periodOffsetFlow = MutableStateFlow(0)
    private var latestRange: WeightReportRange = buildWeightReportRange(WeightReportTab.MONTH, 0)

    val weightGoalKgState = collectStateUI(WeightState::weightGoalKg); val initialWeightKgState = collectStateUI(WeightState::initialWeightKg)
    val currentWeightKgState = collectStateUI(WeightState::currentWeightKg); val selectedTabState = collectStateUI(WeightState::selectedTab)
    val periodTitleState = collectStateUI(WeightState::periodTitle); val showAddWeightSheetState = collectStateUI(WeightState::showAddWeightSheet)
    val draftWeightKgState = collectStateUI(WeightState::draftWeightKg); val editingModeState = collectStateUI(WeightState::editingMode)
    val isSavingWeightState = collectStateUI(WeightState::isSavingWeight); val isLoadingState = collectStateUI(WeightState::isLoading)
    val chartDataState = collectStateUI(WeightState::chartData); val xAxisLabelsState = collectStateUI(WeightState::xAxisLabels)
    val yMinState = collectStateUI(WeightState::yMin); val yMaxState = collectStateUI(WeightState::yMax)
    val historyState = collectStateUI(WeightState::history); val chartStatsState = collectStateUI(WeightState::chartStats)

    init {
        initializeStoredWeights()
        observeLatestWeight()
        observeHistory()
        observeChartRange()
        refreshFromServer()
    }

    fun selectTab(tab: WeightReportTab) {
        if (selectedTabFlow.value == tab) return
        selectedTabFlow.value = tab; periodOffsetFlow.value = 0
    }

    fun nextPeriod() { periodOffsetFlow.value = periodOffsetFlow.value + 1 }

    fun prevPeriod() { periodOffsetFlow.value = periodOffsetFlow.value - 1 }

    fun openGoalWeightSheet() = openWeightSheet(WeightEditingMode.GOAL) { weightGoalKg }

    fun openInitialWeightSheet() = openWeightSheet(WeightEditingMode.INITIAL) {
        if (initialWeightKg > 0f) initialWeightKg else currentWeightKg
    }

    fun openCurrentWeightSheet() = openWeightSheet(WeightEditingMode.CURRENT) {
        if (currentWeightKg > 0f) currentWeightKg else initialWeightKg
    }

    fun closeAddWeightSheet() = setState { copy(showAddWeightSheet = false) }

    fun updateDraftWeight(weightKg: Float) = setState { copy(draftWeightKg = weightKg.coerceIn(MIN_WEIGHT_KG, MAX_WEIGHT_KG)) }

    fun saveDraftWeight() {
        getState { state ->
            if (state.isSavingWeight || state.draftWeightKg <= 0f) return@getState

            setState { copy(isSavingWeight = true) }
            viewModelScope.launch(Dispatchers.IO) {
                saveWeightByMode(state.editingMode, state.draftWeightKg)
                loadRemoteChart(selectedTabFlow.value, latestRange)
                setState { copy(isSavingWeight = false) }
            }
        }
    }

    fun deleteHistory(row: WeightHistoryRow) {
        viewModelScope.launch(Dispatchers.IO) {
            row.serverId?.let { remoteMediator.deleteWeight(it) }
            weightTrackingRepository.delete(row.toEntity())
            loadRemoteChart(selectedTabFlow.value, latestRange)
        }
    }

    fun refreshLocalData() {
        val initialWeight = userProfileSharePref.getInitialWeightKg().toFloat()
        val goalWeight = userProfileSharePref.getWeightGoalKg(AppGoals.WEIGHT_GOAL_KG.toFloat())
        setState {
            copy(
                weightGoalKg = goalWeight,
                initialWeightKg = if (initialWeight > 0f) initialWeight else initialWeightKg
            )
        }
        refreshFromServer()
    }

    private fun initializeStoredWeights() {
        val initialWeight = userProfileSharePref.getInitialWeightKg().toFloat()
        val goalWeight = userProfileSharePref.getWeightGoalKg(AppGoals.WEIGHT_GOAL_KG.toFloat())
        setState {
            copy(
                weightGoalKg = goalWeight,
                initialWeightKg = initialWeight,
                currentWeightKg = initialWeight,
                draftWeightKg = initialWeight
            )
        }
    }

    private fun observeLatestWeight() {
        weightTrackingRepository.observeLatestEntry()
            .onEach { latest ->
                val latestWeight = latest?.weightKg ?: userProfileSharePref.getInitialWeightKg().toFloat()
                setState {
                    copy(
                        currentWeightKg = if (latestWeight > 0f) latestWeight else currentWeightKg,
                        draftWeightKg = if (draftWeightKg <= 0f && latestWeight > 0f) latestWeight else draftWeightKg
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeHistory() {
        weightTrackingRepository.observeRecentEntries()
            .onEach { entries -> setState { copy(history = entries.map { it.toHistoryRow() }) } }
            .launchIn(viewModelScope)
    }

    private fun observeChartRange() {
        combine(selectedTabFlow, periodOffsetFlow) { tab, offset -> tab to buildWeightReportRange(tab, offset) }
            .flatMapLatest { (tab, range) ->
                latestRange = range
                weightTrackingRepository.observeEntriesInRange(range.startMillis, range.endMillis)
                    .onEach { entries -> applyLocalChart(tab, range, entries) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun applyLocalChart(
        tab: WeightReportTab,
        range: WeightReportRange,
        entries: List<WeightEntryEntity>
    ) {
        val chart = buildWeightChart(tab, entries, range, currentStateGoal())
        setState {
            copy(
                selectedTab = tab,
                periodTitle = range.title,
                chartData = chart.points,
                xAxisLabels = chart.xAxisLabels,
                yMin = chart.yMin,
                yMax = chart.yMax,
                chartStats = null
            )
        }
        loadRemoteChart(tab, range)
    }

    private fun openWeightSheet(mode: WeightEditingMode, draftValue: WeightState.() -> Float) = setState {
        copy(showAddWeightSheet = true, editingMode = mode, draftWeightKg = draftValue())
    }

    private suspend fun saveWeightByMode(mode: WeightEditingMode, weightKg: Float) {
        when (mode) {
            WeightEditingMode.GOAL -> saveGoalWeight(weightKg)
            WeightEditingMode.INITIAL -> saveInitialWeight(weightKg)
            WeightEditingMode.CURRENT -> saveCurrentWeight(weightKg)
        }
    }

    private fun saveGoalWeight(weightKg: Float) {
        userProfileSharePref.saveWeightGoalKg(weightKg)
        setState { copy(weightGoalKg = weightKg, showAddWeightSheet = false) }
    }

    private suspend fun saveInitialWeight(weightKg: Float) {
        if (!syncInitialWeightToServer(weightKg)) return
        userProfileSharePref.saveInitialWeightKg(weightKg.toInt())
        setState {
            copy(
                initialWeightKg = weightKg,
                currentWeightKg = if (currentWeightKg <= 0f) weightKg else currentWeightKg,
                showAddWeightSheet = false
            )
        }
    }

    private suspend fun saveCurrentWeight(weightKg: Float) {
        val serverId = syncCurrentWeightToServer(weightKg)
        weightTrackingRepository.saveDailyWeight(weightKg, serverId = serverId, note = DEFAULT_WEIGHT_NOTE)
        setState { copy(currentWeightKg = weightKg, showAddWeightSheet = false) }
    }

    private fun refreshFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            setState { copy(isLoading = true) }
            refreshRemoteHistory()
            refreshRemoteCurrentWeight()
            loadRemoteChart(selectedTabFlow.value, latestRange)
            setState { copy(isLoading = false) }
        }
    }

    private suspend fun refreshRemoteHistory() {
        remoteMediator.fetchHistory(HISTORY_LIMIT)
            ?.let { weightTrackingRepository.replaceAll(it) }
    }

    private suspend fun refreshRemoteCurrentWeight() {
        remoteMediator.fetchCurrentWeight()?.let { current -> setState { copy(currentWeightKg = current) } }
    }

    private suspend fun syncInitialWeightToServer(weightKg: Float): Boolean = remoteMediator.syncInitialWeight(weightKg)

    private suspend fun syncCurrentWeightToServer(weightKg: Float): Long? = remoteMediator.syncCurrentWeight(weightKg)

    private suspend fun loadRemoteChart(tab: WeightReportTab, range: WeightReportRange) {
        val result = remoteMediator.fetchChart(tab, range, currentStateGoal()) ?: return
        setState {
            copy(
                chartData = result.chart.points,
                xAxisLabels = result.chart.xAxisLabels,
                yMin = result.chart.yMin,
                yMax = result.chart.yMax,
                chartStats = result.stats
            )
        }
    }

    private fun currentStateGoal(): Float {
        var goal = AppGoals.WEIGHT_GOAL_KG.toFloat()
        getState { goal = it.weightGoalKg.takeIf { value -> value > 0f } ?: goal }
        return goal
    }
}

private const val HISTORY_LIMIT = 30
private const val MIN_WEIGHT_KG = 25f
private const val MAX_WEIGHT_KG = 200f
internal const val DEFAULT_WEIGHT_NOTE = "Sau khi ăn sáng"
