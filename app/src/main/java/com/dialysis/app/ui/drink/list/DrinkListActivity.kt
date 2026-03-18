package com.dialysis.app.ui.drink.list

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router

class DrinkListActivity : BaseActivity() {

    @Composable
    override fun ContentView() {
        DrinkListScreen(
            onDrinkClick = { drinkName ->
                startActivity(Router.createDrink(this, drinkName))
            }
        )
    }
}
