package com.dialysis.app.ui.login

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity() {
    private val viewModel: LoginViewModel by viewModel()

    @Composable
    override fun ContentView() {
        LoginScreen(viewModel = viewModel) {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.flowOf(LoginState::isLoginSuccess)
                        .collect { isLoginSuccess ->
                            if (isLoginSuccess) {
                                startActivity(Router.home(this@LoginActivity))
                                finish()
                            }
                        }
                }
            }
        }
    }
}
