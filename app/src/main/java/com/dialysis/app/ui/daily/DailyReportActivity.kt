package com.dialysis.app.ui.daily

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class DailyReportActivity : BaseActivity() {
    private val viewModel: DailyReportViewModel by viewModel()

    @Composable
    override fun ContentView() {
        DailyReportScreen(viewModel)
    }
}
