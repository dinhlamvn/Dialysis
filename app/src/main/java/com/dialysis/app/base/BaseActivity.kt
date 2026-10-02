package com.dialysis.app.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dialysis.app.ui.theme.AppTheme
import com.dialysis.app.util.InAppUpdateHelper

abstract class BaseActivity : AppCompatActivity() {
    protected open val checkForNewUpdates: Boolean = false

    @Composable
    abstract fun ContentView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InAppUpdateHelper(this, checkForNewUpdates = checkForNewUpdates)
        setContent {
            AppTheme {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ContentView()
                    }
                }
            }
        }
    }
}
