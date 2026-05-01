package com.dialysis.app.ui.home.tabs

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.config.AppGoals
import com.dialysis.app.ui.statistics.StatisticsScreen
import com.dialysis.app.ui.theme.AppTheme

class StatisticsFragment : BaseFragment() {
    @Composable
    override fun ContentView() {
        AppTheme {
            StatisticsScreen(
                waterEntries = emptyList(),
                dailyGoalMl = AppGoals.DAILY_WATER_GOAL_ML,
                dailyReportViewModel = null
            )
        }
    }
}
