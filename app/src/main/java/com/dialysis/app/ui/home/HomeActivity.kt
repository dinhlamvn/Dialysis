package com.dialysis.app.ui.home

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity

class HomeActivity : BaseActivity() {
    private val viewModel: HomeViewModel by viewModels()

    @Composable
    override fun ContentView() {
        HomeScreen(viewModel)
    }
}
