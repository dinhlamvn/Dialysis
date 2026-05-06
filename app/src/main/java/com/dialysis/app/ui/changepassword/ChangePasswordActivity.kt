package com.dialysis.app.ui.changepassword

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePasswordActivity : BaseActivity() {
    private val viewModel: ChangePasswordViewModel by viewModel()

    @Composable
    override fun ContentView() {
        ChangePasswordScreen(viewModel = viewModel) {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setResetData(
            identifier = intent?.getStringExtra(Router.EXTRA_RESET_IDENTIFIER).orEmpty(),
            otpCode = intent?.getStringExtra(Router.EXTRA_RESET_OTP_CODE).orEmpty()
        )
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.flowOf(ChangePasswordState::isSuccess).collect { isSuccess ->
                    if (isSuccess) {
                        startActivity(Router.resetPasswordSuccess(this@ChangePasswordActivity))
                        finish()
                    }
                }
            }
        }
    }
}
