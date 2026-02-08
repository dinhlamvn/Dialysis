package com.dialysis.app.router

import android.content.Context
import android.content.Intent
import com.dialysis.app.ui.register.RegisterActivity

object Router {
    fun register(context: Context): Intent {
        return Intent(context, RegisterActivity::class.java)
    }
}