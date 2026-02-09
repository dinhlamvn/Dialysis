package com.dialysis.app.router

import android.content.Context
import android.content.Intent
import com.dialysis.app.ui.daily.DailyReportActivity
import com.dialysis.app.ui.drink.create.CreateDrinkActivity
import com.dialysis.app.ui.drink.list.DrinkListActivity
import com.dialysis.app.ui.home.HomeActivity
import com.dialysis.app.ui.register.RegisterActivity

object Router {
    fun register(context: Context): Intent {
        return Intent(context, RegisterActivity::class.java)
    }

    fun home(context: Context): Intent {
        return Intent(context, HomeActivity::class.java)
    }

    fun dailyReport(context: Context): Intent {
        return Intent(context, DailyReportActivity::class.java)
    }

    fun drinkList(context: Context): Intent {
        return Intent(context, DrinkListActivity::class.java)
    }

    fun createDrink(context: Context): Intent {
        return Intent(context, CreateDrinkActivity::class.java)
    }
}
