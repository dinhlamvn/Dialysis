package com.dialysis.app.ui.forgotpassword

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPasswordActivity : BaseActivity() {
    private val viewModel: ForgotPasswordViewModel by viewModel()

    @Composable
    override fun ContentView() {
        ForgotPasswordScreen(viewModel = viewModel) {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.flowOf { it }.collect { state ->
                    if (state.isOtpSent) {
                        startActivity(
                            Router.resetOtpVerify(
                                this@ForgotPasswordActivity,
                                state.successIdentifierType,
                                state.successIdentifier
                            )
                        )
                        finish()
                    }
                }
            }
        }
    }
}
