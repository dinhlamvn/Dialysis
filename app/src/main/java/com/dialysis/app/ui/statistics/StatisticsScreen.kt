package com.dialysis.app.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.ui.daily.DailyReportScreen
import com.dialysis.app.ui.daily.DailyReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    waterEntries: List<WaterEntryEntity>,
    dailyGoalMl: Int,
    dailyReportViewModel: DailyReportViewModel?
) {
    var activeTab by rememberSaveable { mutableStateOf(StatisticsTab.MAIN) }
    var beveragePeriod by rememberSaveable { mutableStateOf(BeverageFilterPeriod.LAST_7_DAYS) }
    var selectedReportDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedMonthStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state = remember(waterEntries, dailyGoalMl, beveragePeriod) {
        StatisticsCalculator.build(waterEntries, dailyGoalMl, beveragePeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        StatisticsTabSelector(activeTab = activeTab, onSelect = { activeTab = it })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when (activeTab) {
                    StatisticsTab.MAIN -> StatisticsMainTab(
                        state = state,
                        onDayClick = { selectedReportDateMillis = it },
                        onMonthClick = { selectedMonthStartMillis = it }
                    )
                    StatisticsTab.BEVERAGES -> StatisticsBeveragesTab(
                        stats = state.beverageStats,
                        totalMl = state.totalConsumptionMl,
                        selectedPeriod = beveragePeriod,
                        onPeriodChange = { beveragePeriod = it }
                    )
                    StatisticsTab.BY_DAY -> StatisticsByDayTab(
                        dailyStats = state.dailyStats,
                        onDayClick = { selectedReportDateMillis = it }
                    )
                    StatisticsTab.MONTHLY -> StatisticsMonthlyTab(
                        summaries = state.monthSummaries,
                        onMonthClick = { selectedMonthStartMillis = it }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }

    if (selectedReportDateMillis != null && dailyReportViewModel != null) {
        ModalBottomSheet(
            sheetState = reportSheetState,
            onDismissRequest = {
                selectedReportDateMillis = null
                dailyReportViewModel.showTodayReport()
            }
        ) {
            dailyReportViewModel.showDateReport(selectedReportDateMillis ?: System.currentTimeMillis())
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.88f)) {
                DailyReportScreen(viewModel = dailyReportViewModel, onBackClick = {
                    selectedReportDateMillis = null
                    dailyReportViewModel.showTodayReport()
                })
            }
        }
    }

    selectedMonthStartMillis?.let { monthStart ->
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { selectedMonthStartMillis = null }
        ) {
            StatisticsMonthDetail(
                monthStartMillis = monthStart,
                monthDays = StatisticsCalculator.monthDays(waterEntries, dailyGoalMl, monthStart),
                onDayClick = { selectedReportDateMillis = it }
            )
        }
    }
}

@Composable
private fun StatisticsTabSelector(
    activeTab: StatisticsTab,
    onSelect: (StatisticsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatisticsTab.entries.forEach { tab ->
            Text(
                text = tab.title,
                style = TextStyle(fontSize = 14.sp, fontWeight = if (activeTab == tab) FontWeight.SemiBold else FontWeight.Medium),
                color = if (activeTab == tab) Color.White else Color(0xFF1F2633),
                modifier = Modifier
                    .background(if (activeTab == tab) Color(0xFF1877F2) else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
