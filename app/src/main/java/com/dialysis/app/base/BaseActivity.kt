package com.dialysis.app.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.dialysis.app.R
import com.dialysis.app.data.network.auth.SessionExpiredNotifier
import com.dialysis.app.router.Router
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.ui.theme.AppTheme
import com.dialysis.app.util.InAppUpdateHelper
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {
    protected open val checkForNewUpdates: Boolean = false

    /** Screens of the login/register flow set this to false: there is no session to expire there. */
    protected open val showsSessionExpiredDialog: Boolean = true

    private val sessionExpiredNotifier: SessionExpiredNotifier by inject()
    private val accountSharePref: AccountSharePref by inject()

    @Composable
    abstract fun ContentView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InAppUpdateHelper(this, checkForNewUpdates = checkForNewUpdates)
        setContent {
            AppTheme {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ContentView()
                    }
                }
                if (showsSessionExpiredDialog) {
                    SessionExpiredDialog()
                }
            }
        }
    }

    @Composable
    private fun SessionExpiredDialog() {
        val expired by sessionExpiredNotifier.expired.collectAsState()
        if (!expired) return
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            text = { Text(stringResource(R.string.session_expired_message)) },
            confirmButton = {
                TextButton(onClick = ::onSessionExpiredConfirmed) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }

    /** Clears only the token (pending local records stay) and opens the login screen. */
    private fun onSessionExpiredConfirmed() {
        accountSharePref.clearSession()
        sessionExpiredNotifier.acknowledge()
        startActivity(Router.loginAfterSessionExpired(this))
    }
}
