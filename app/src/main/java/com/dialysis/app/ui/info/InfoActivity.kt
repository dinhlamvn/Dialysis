package com.dialysis.app.ui.info

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dialysis.app.R
import com.dialysis.app.base.BaseActivity
import com.dialysis.app.router.Router
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InfoActivity : BaseActivity() {

    private val viewModel: InfoViewModel by viewModel()

    @Composable
    override fun ContentView() {
        InfoScreen(viewModel)  {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.flowOf(InfoState::calculateGoalStatus).collect { status ->
                        when (status) {
                            is CalculateGoalStatus.None -> Unit
                            is CalculateGoalStatus.Success -> {
                                startActivity(Router.home(this@InfoActivity))
                                finish()
                            }
                            is CalculateGoalStatus.Failed -> {
                                viewModel.clearCalculateGoalStatus()
                                showCalculateGoalFailedAlert(status.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showCalculateGoalFailedAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.info_calculate_goal_failed_title))
            .setMessage("$message\n\n${getString(R.string.info_calculate_goal_failed_retry_suffix)}")
            .setNegativeButton(getString(R.string.common_cancel), null)
            .setPositiveButton(getString(R.string.common_retry)) { _, _ ->
                viewModel.retryCalculateGoal()
            }
            .show()
    }
}
