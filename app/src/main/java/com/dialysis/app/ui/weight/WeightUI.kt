package com.dialysis.app.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dialysis.app.ui.components.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    viewModel: WeightViewModel,
    showBottomNav: Boolean = true
) {
    val weightGoalKg by viewModel.weightGoalKgState.collectAsStateWithLifecycle()
    val initialWeightKg by viewModel.initialWeightKgState.collectAsStateWithLifecycle()
    val currentWeightKg by viewModel.currentWeightKgState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTabState.collectAsStateWithLifecycle()
    val periodTitle by viewModel.periodTitleState.collectAsStateWithLifecycle()
    val chartData by viewModel.chartDataState.collectAsStateWithLifecycle()
    val xAxisLabels by viewModel.xAxisLabelsState.collectAsStateWithLifecycle()
    val yMin by viewModel.yMinState.collectAsStateWithLifecycle()
    val yMax by viewModel.yMaxState.collectAsStateWithLifecycle()
    val showSheet by viewModel.showAddWeightSheetState.collectAsStateWithLifecycle()
    val draftWeightKg by viewModel.draftWeightKgState.collectAsStateWithLifecycle()
    val editingMode by viewModel.editingModeState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSavingWeightState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingState.collectAsStateWithLifecycle()
    val history by viewModel.historyState.collectAsStateWithLifecycle()
    val chartStats by viewModel.chartStatsState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeightScreenBackground)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeightGoalHeader(weightGoalKg = weightGoalKg, onEditClick = viewModel::openGoalWeightSheet)
            }
            item {
                WeightInfoCards(
                    initialWeightKg = initialWeightKg,
                    currentWeightKg = currentWeightKg,
                    progressKg = currentWeightKg - initialWeightKg,
                    onEditInitialWeightClick = viewModel::openInitialWeightSheet
                )
            }
            item {
                WeightReportTabBar(
                    selectedTab = selectedTab,
                    onMonthClick = { viewModel.selectTab(WeightReportTab.MONTH) },
                    onYearClick = { viewModel.selectTab(WeightReportTab.YEAR) }
                )
            }
            item {
                WeightPeriodNavigator(title = periodTitle, onPrev = viewModel::prevPeriod, onNext = viewModel::nextPeriod)
            }
            item {
                WeightChartCard(
                    xAxisLabels = xAxisLabels,
                    chartData = chartData,
                    yMin = yMin,
                    yMax = yMax,
                    goalWeightKg = weightGoalKg,
                    chartStats = chartStats
                )
            }
            item {
                WeightHistorySection(history = history, onDelete = viewModel::deleteHistory)
            }
            item {
                Button(
                    onClick = viewModel::openCurrentWeightSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WeightAccentBlue)
                ) {
                    Text("Thêm cân nặng hiện tại", color = WeightWhite, style = TextStyle(fontSize = 16.sp))
                }
            }
        }

        if (showBottomNav) {
            WeightBottomNav()
        }
    }

    if (showSheet) {
        ModalBottomSheet(sheetState = bottomSheetState, onDismissRequest = viewModel::closeAddWeightSheet) {
            WeightEditSheet(
                editingMode = editingMode,
                draftWeightKg = draftWeightKg,
                onCancel = viewModel::closeAddWeightSheet,
                onSave = viewModel::saveDraftWeight,
                onWeightChange = viewModel::updateDraftWeight
            )
        }
    }

    if (isSaving || isLoading) {
        Loading(overlayColor = WeightOverlayScrim)
    }
}
