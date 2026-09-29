package com.dialysis.app.ui.info

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.R
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InfoActivity : BaseActivity() {

    private val viewModel: InfoViewModel by viewModel()

    @Composable
    override fun ContentView() {
        InfoScreen(viewModel)  {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadInitialData()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.shouldOpenHomeState.collect { shouldOpen ->
                        if (!shouldOpen) return@collect
                        viewModel.consumeOpenHomeEvent()
                        startActivity(Router.home(this@InfoActivity))
                        finish()
                    }
                }
            }
        }
    }

}
