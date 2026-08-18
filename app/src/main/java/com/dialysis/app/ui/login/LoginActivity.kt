package com.dialysis.app.ui.login

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.flow.combine
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
                    combine(
                        viewModel.flowOf(LoginState::isLoginSuccess),
                        viewModel.flowOf(LoginState::requiresInfoCompletion)
                    ) { isLoginSuccess, requiresInfoCompletion ->
                        isLoginSuccess to requiresInfoCompletion
                    }.collect { (isLoginSuccess, requiresInfoCompletion) ->
                            if (isLoginSuccess) {
                                val destination = if (requiresInfoCompletion) {
                                    Router.infoAfterAuth(this@LoginActivity)
                                } else {
                                    Router.homeAfterAuth(this@LoginActivity)
                                }
                                startActivity(destination)
                                finish()
                                }
                        }
                }
            }
        }
    }
}
