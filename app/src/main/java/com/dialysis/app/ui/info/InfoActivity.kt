package com.dialysis.app.ui.info

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class InfoActivity : BaseActivity() {

    private val viewModel: InfoViewModel by viewModel()

    @Composable
    override fun ContentView() {
        InfoScreen(viewModel)
    }
}
