package com.dialysis.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dialysis.app.R

/** The one popup for user actions that need a connection while the device is offline. */
@Composable
fun NoInternetDialog(visible: Boolean, onDismiss: () -> Unit) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(R.string.no_internet_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_ok))
            }
        }
    )
}
