package com.dialysis.app.ui.changepassword

import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router

class ResetPasswordSuccessActivity : BaseActivity() {
    override val showsSessionExpiredDialog = false

    @Composable
    override fun ContentView() {
        ResetPasswordSuccessScreen {
            startActivity(Router.loginAfterPasswordReset(this))
            finish()
        }
    }
}
