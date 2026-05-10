package com.dialysis.app.ui.otpverify

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtpVerifyActivity : BaseActivity() {
    private val viewModel: OtpVerifyViewModel by viewModel()

    @Composable
    override fun ContentView() {
        OtpVerifyScreen(viewModel = viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val identifierType = intent?.getStringExtra(Router.EXTRA_OTP_IDENTIFIER_TYPE).orEmpty()
        val identifier = intent?.getStringExtra(Router.EXTRA_OTP_IDENTIFIER).orEmpty()
        val mode = intent?.getStringExtra(Router.EXTRA_OTP_MODE).orEmpty()
        viewModel.setIdentifierData(identifierType = identifierType, identifier = identifier, mode = mode)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.flowOf { it }
                        .collect { state ->
                            if (state.isVerifySuccess) {
                                if (state.mode == Router.OTP_MODE_PASSWORD_RESET) {
                                    startActivity(
                                        Router.changePassword(
                                            this@OtpVerifyActivity,
                                            state.identifier,
                                            state.otpCode
                                        )
                                    )
                                } else {
                                    startActivity(Router.home(this@OtpVerifyActivity))
                                }
                                finish()
                            }
                        }
                }
            }
        }
    }
}
