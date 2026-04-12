package com.dialysis.app.ui.home.tabs

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.ui.theme.AppTheme
import com.dialysis.app.ui.weight.WeightScreen
import com.dialysis.app.ui.weight.WeightViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WeightTabFragment : BaseFragment() {
    private val viewModel: WeightViewModel by activityViewModel()

    override fun onResume() {
        super.onResume()
        viewModel.refreshLocalData()
    }

    @Composable
    override fun ContentView() {
        AppTheme {
            WeightScreen(
                viewModel = viewModel,
                showBottomNav = false
            )
        }
    }
}
