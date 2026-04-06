package com.dialysis.app.ui.register

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    @Composable
    override fun ContentView() {
        RegisterScreen(viewModel) {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.flowOf { it }
                        .collect { state ->
                            if (state.isRegisterSuccess) {
                                val identifier = state.emailOrPhone.trim()
                                val identifierType = if (identifier.contains("@")) "email" else "phone"
                                startActivity(
                                    Router.otpVerify(
                                        context = this@RegisterActivity,
                                        identifierType = identifierType,
                                        identifier = identifier
                                    )
                                )
                                finish()
                            }
                        }
                }
            }
        }
    }
}
