package com.dialysis.app.ui.info

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity

class InfoActivity : BaseActivity() {

    private val viewModel: InfoViewModel by viewModels()

    @Composable
    override fun ContentView() {
        InfoScreen(viewModel)
    }
}
