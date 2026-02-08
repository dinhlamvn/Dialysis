package com.dialysis.app.ui.main

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.ui.intro.IntroScreen

class MainActivity : BaseActivity() {

    @Composable
    override fun ContentView() {
        IntroScreen()
    }
}
