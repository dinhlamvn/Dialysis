package com.dialysis.app.ui.home.tabs

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.home.HomeScreen
import com.dialysis.app.ui.home.HomeViewModel
import com.dialysis.app.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WaterFragment : BaseFragment() {
    private val viewModel: HomeViewModel by activityViewModel()
    private val dailyReportViewModel: DailyReportViewModel by activityViewModel()

    @Composable
    override fun ContentView() {
        AppTheme {
            HomeScreen(
                viewModel = viewModel,
                dailyReportViewModel = dailyReportViewModel,
                showBottomNav = false
            )
        }
    }
}
