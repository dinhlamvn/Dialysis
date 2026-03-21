package com.dialysis.app.ui.home.tabs

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.ui.theme.AppTheme
import com.dialysis.app.ui.weight.WeightScreen

class WeightTabFragment : BaseFragment() {
    @Composable
    override fun ContentView() {
        AppTheme {
            WeightScreen(showBottomNav = false)
        }
    }
}
