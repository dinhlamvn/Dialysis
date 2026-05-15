package com.dialysis.app.ui.register

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : BaseActivity() {
    private val viewModel: RegisterViewModel by viewModel()

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
                                val email = state.email.trim()
                                val phone = state.phone.trim()
                                val identifier = email.takeIf { it.isNotBlank() } ?: phone
                                val identifierType = if (email.isNotBlank()) "email" else "phone"
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
