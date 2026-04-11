package com.dialysis.app.router

import android.content.Context
import android.content.Intent
import com.dialysis.app.ui.daily.DailyReportActivity
import com.dialysis.app.ui.drink.create.CreateDrinkActivity
import com.dialysis.app.ui.drink.list.DrinkListActivity
import com.dialysis.app.ui.home.HomeActivity
import com.dialysis.app.ui.info.InfoActivity
import com.dialysis.app.ui.login.LoginActivity
import com.dialysis.app.ui.otpverify.OtpVerifyActivity
import com.dialysis.app.ui.register.RegisterActivity
import com.dialysis.app.ui.weight.WeightActivity

object Router {
    const val EXTRA_OTP_IDENTIFIER_TYPE = "extra_otp_identifier_type"
    const val EXTRA_OTP_IDENTIFIER = "extra_otp_identifier"
    const val EXTRA_DRINK_NAME = "extra_drink_name"

    fun register(context: Context): Intent {
        return Intent(context, RegisterActivity::class.java)
    }

    fun info(context: Context): Intent {
        return Intent(context, InfoActivity::class.java)
    }

    fun home(context: Context): Intent {
        return Intent(context, HomeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    fun login(context: Context): Intent {
        return Intent(context, LoginActivity::class.java)
    }

    fun weight(context: Context): Intent {
        return Intent(context, WeightActivity::class.java)
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

    fun createDrink(context: Context, drinkName: String): Intent {
        return Intent(context, CreateDrinkActivity::class.java).apply {
            putExtra(EXTRA_DRINK_NAME, drinkName)
        }
    }

    fun otpVerify(context: Context): Intent {
        return Intent(context, OtpVerifyActivity::class.java)
    }

    fun otpVerify(context: Context, identifierType: String, identifier: String): Intent {
        return Intent(context, OtpVerifyActivity::class.java).apply {
            putExtra(EXTRA_OTP_IDENTIFIER_TYPE, identifierType)
            putExtra(EXTRA_OTP_IDENTIFIER, identifier)
        }
    }
}
