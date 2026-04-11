package com.dialysis.app.ui.main

import android.Manifest
import android.os.Bundle
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.dialysis.app.R
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.extensions.toast
import com.dialysis.app.router.Router
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.ui.intro.IntroScreen
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private val accountSharePref: AccountSharePref by inject()
    private val userProfileSharePref: UserProfileSharePref by inject()
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            startHome()
        } else {
            toast(getString(R.string.permission_notification_required))
            startHome()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
    }

    @Composable
    override fun ContentView() {
        IntroScreen()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startHome()
        }
    }

    private fun startHome() {
        if (accountSharePref.getToken().isNotBlank() || userProfileSharePref.getProfile() != null) {
            startActivity(Router.home(this))
            finish()
            return
        }
    }
}
