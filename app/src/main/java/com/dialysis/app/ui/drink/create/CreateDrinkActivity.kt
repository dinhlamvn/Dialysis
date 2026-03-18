package com.dialysis.app.ui.drink.create

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateDrinkActivity : BaseActivity() {
    private val viewModel: CreateDrinkViewModel by viewModel()

    @Composable
    override fun ContentView() {
        val drinkName by viewModel.drinkNameState.collectAsStateWithLifecycle()
        CreateDrinkScreen(
            drinkName = drinkName,
            viewModel = viewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drinkName = intent?.getStringExtra(Router.EXTRA_DRINK_NAME).orEmpty()
        viewModel.updateDrinkName(drinkName)
    }
}
