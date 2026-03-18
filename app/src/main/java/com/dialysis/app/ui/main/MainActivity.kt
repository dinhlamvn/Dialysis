package com.dialysis.app.ui.main

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.ui.intro.IntroScreen
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private val accountSharePref: AccountSharePref by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (accountSharePref.getToken().isNotBlank()) {
            startActivity(Router.home(this))
            finish()
        }
    }

    @Composable
    override fun ContentView() {
        IntroScreen()
    }
}
