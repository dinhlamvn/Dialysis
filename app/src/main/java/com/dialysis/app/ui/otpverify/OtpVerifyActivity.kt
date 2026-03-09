package com.dialysis.app.ui.otpverify

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router

class OtpVerifyActivity : BaseActivity() {
    private val viewModel: OtpVerifyViewModel by viewModels()

    @Composable
    override fun ContentView() {
        OtpVerifyScreen(viewModel = viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val identifierType = intent?.getStringExtra(Router.EXTRA_OTP_IDENTIFIER_TYPE).orEmpty()
        val identifier = intent?.getStringExtra(Router.EXTRA_OTP_IDENTIFIER).orEmpty()
        viewModel.setIdentifierData(identifierType = identifierType, identifier = identifier)
    }
}
