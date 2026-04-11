package com.dialysis.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.R
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.extensions.toast
import com.dialysis.app.router.Router
import com.dialysis.app.ui.intro.IntroScreen
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroAcitivity : BaseActivity() {
    private val introViewModel: IntroViewModel by viewModel()
    private var hasNavigatedToHome = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (!result) {
            toast(getString(R.string.permission_notification_required))
        }
        introViewModel.startAppFlow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeState()
        requestNotificationPermissionIfNeeded()
    }

    @Composable
    override fun ContentView() {
        IntroScreen()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    introViewModel.shouldOpenHomeState.collect { shouldOpenHome ->
                        if (!shouldOpenHome) return@collect
                        introViewModel.consumeOpenHomeEvent()
                        openHome()
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            introViewModel.startAppFlow()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            introViewModel.startAppFlow()
        }
    }

    private fun openHome() {
        if (hasNavigatedToHome || isFinishing) return
        hasNavigatedToHome = true
        startActivity(Router.home(this))
        finish()
    }
}
