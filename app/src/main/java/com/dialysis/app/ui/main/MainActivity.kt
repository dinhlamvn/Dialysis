package com.dialysis.app.ui.main

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.ui.intro.IntroScreen
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private val accountSharePref: AccountSharePref by inject()
    private val userProfileSharePref: UserProfileSharePref by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (accountSharePref.getToken().isNotBlank() || userProfileSharePref.getProfile() != null) {
            startActivity(Router.home(this))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun ContentView() {
        IntroScreen()
    }
}
