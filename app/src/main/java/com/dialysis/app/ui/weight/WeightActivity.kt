package com.dialysis.app.ui.weight

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeightActivity : BaseActivity() {
    private val viewModel: WeightViewModel by viewModel()

    @Composable
    override fun ContentView() {
        WeightScreen(viewModel = viewModel)
    }
}
