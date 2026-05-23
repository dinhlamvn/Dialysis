package com.dialysis.app.ui.home.tabs

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dialysis.app.BuildConfig
import com.dialysis.app.R
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : BaseFragment() {
    private val settingsViewModel: SettingsViewModel by viewModel()

    @Composable
    override fun ContentView() {
        AppTheme {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    var showAccountDetails by rememberSaveable { mutableStateOf(false) }
    val isLoadingAccount = viewModel.isLoadingAccountState.collectAsStateWithLifecycle().value
    val accountContact = viewModel.accountContactState.collectAsStateWithLifecycle().value
    val isLoggedIn = viewModel.isLoggedInState.collectAsStateWithLifecycle().value
    val lastWaterSyncAt = viewModel.lastWaterSyncAtState.collectAsStateWithLifecycle().value
    val showDeleteAccountConfirm = viewModel.showDeleteAccountConfirmState.collectAsStateWithLifecycle().value
    val isDeletingAccount = viewModel.isDeletingAccountState.collectAsStateWithLifecycle().value
    val deleteAccountError = viewModel.deleteAccountErrorState.collectAsStateWithLifecycle().value
    val deleteAccountErrorResId = viewModel.deleteAccountErrorResIdState.collectAsStateWithLifecycle().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (showAccountDetails && isLoggedIn) {
                item {
                    AccountDetailsContent(
                        onDeleteAccountClick = viewModel::requestDeleteAccount,
                        onBackClick = { showAccountDetails = false }
                    )
                }
            } else {
                item {
                    SettingsMainContent(
                        isLoadingAccount = isLoadingAccount,
                        accountContact = accountContact,
                        isLoggedIn = isLoggedIn,
                        onAccountClick = { showAccountDetails = true },
                        onSignInClick = { context.startActivity(Router.login(context)) },
                        onSignOutClick = viewModel::signOut
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        Text(
            text = lastSyncText(context, lastWaterSyncAt),
            style = TextStyles.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
        )

        if (showDeleteAccountConfirm) {
            DeleteAccountConfirmDialog(
                isDeleting = isDeletingAccount,
                onDismiss = viewModel::dismissDeleteAccountConfirm,
                onConfirm = viewModel::confirmDeleteAccount
            )
        }

        if (deleteAccountError != null || deleteAccountErrorResId != null) {
            DeleteAccountErrorDialog(
                message = deleteAccountError
                    ?: stringResource(deleteAccountErrorResId ?: R.string.settings_delete_account_failed_message),
                onDismiss = viewModel::clearDeleteAccountError
            )
        }
    }
}

@Composable
private fun SettingsMainContent(
    isLoadingAccount: Boolean,
    accountContact: String?,
    isLoggedIn: Boolean,
    onAccountClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SettingsTitle(title = stringResource(R.string.settings_title))
        if (isLoggedIn) {
            AccountEntrySection(onAccountClick = onAccountClick)
        }
        AppInformationSection()
        NotificationSection()
        PreferencesSection(
            isLoadingAccount = isLoadingAccount,
            accountContact = accountContact,
            isLoggedIn = isLoggedIn,
            onSignInClick = onSignInClick,
            onSignOutClick = onSignOutClick
        )
    }
}

@Composable
private fun AccountDetailsContent(
    onDeleteAccountClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        AccountDetailsHeader(onBackClick = onBackClick)
        AccountDeleteSection(onDeleteAccountClick = onDeleteAccountClick)
    }
}

@Composable
private fun SettingsTitle(title: String) {
    Text(
        text = title,
        style = TextStyles.titleMedium,
        color = TextDark,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp)
    )
}

@Composable
private fun AccountEntrySection(
    onAccountClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionHeader(stringResource(R.string.settings_account))
        SettingsCard {
            SettingsRow(
                data = SettingsRowData(
                    title = stringResource(R.string.settings_account),
                    showChevron = true
                ),
                onClick = onAccountClick
            )
        }
    }
}

@Composable
private fun AccountDetailsHeader(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = stringResource(R.string.common_back),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(R.string.settings_account),
            style = TextStyles.titleMedium,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AccountDeleteSection(
    onDeleteAccountClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionHeader(stringResource(R.string.settings_delete_account))
        SettingsCard {
            SettingsRow(
                data = SettingsRowData(
                    title = stringResource(R.string.settings_delete_account),
                    showChevron = true,
                    isDestructive = true
                ),
                onClick = onDeleteAccountClick
            )
        }
    }
}

@Composable
private fun AppInformationSection() {
    SettingsSection(
        title = stringResource(R.string.settings_app_information),
        rows = listOf(
            SettingsRowData(
                title = stringResource(R.string.settings_version),
                value = normalizedVersion(BuildConfig.VERSION_NAME)
            )
        )
    )
}

@Composable
private fun NotificationSection() {
    SettingsSection(
        title = stringResource(R.string.settings_notification),
        rows = listOf(
            SettingsRowData(
                title = stringResource(R.string.settings_notification_title),
                showChevron = true
            )
        )
    )
}

@Composable
private fun PreferencesSection(
    isLoadingAccount: Boolean,
    accountContact: String?,
    isLoggedIn: Boolean,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionHeader(stringResource(R.string.settings_preferences))
        SettingsCard {
            when {
                isLoadingAccount -> AccountLoadingRow()
                isLoggedIn -> SettingsRow(
                    data = SettingsRowData(
                        title = stringResource(R.string.settings_sign_out),
                        value = accountContact.orEmpty(),
                        isDestructive = true
                    ),
                    onClick = onSignOutClick
                )
                else -> SettingsRow(
                    data = SettingsRowData(
                        title = stringResource(R.string.settings_sign_in),
                        showChevron = true
                    ),
                    onClick = onSignInClick
                )
            }
        }
    }
}

@Composable
private fun AccountLoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            color = AccentBlue,
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    rows: List<SettingsRowData>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionHeader(title)
        SettingsCard {
            rows.forEachIndexed { index, row ->
                SettingsRow(data = row)
                if (index != rows.lastIndex) {
                    SettingsDivider()
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyles.bodyMedium,
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SettingsRow(
    data: SettingsRowData,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = data.title,
            style = TextStyles.title,
            color = if (data.isDestructive) DestructiveRed else TextDark
        )
        Spacer(modifier = Modifier.weight(1f))
        if (data.value.isNotBlank()) {
            Text(
                text = data.value,
                style = TextStyles.body,
                color = TextMuted,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        if (data.showChevron) {
            Text(
                text = ">",
                style = TextStyles.titleMedium,
                color = ChevronColor,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .padding(start = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor)
    )
}

@Composable
private fun DeleteAccountConfirmDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_delete_account_confirm_title),
                style = TextStyles.titleMedium,
                color = TextDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.settings_delete_account_confirm_message),
                    style = TextStyles.body,
                    color = TextMuted
                )
                if (isDeleting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = DestructiveRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_delete_account_deleting),
                            style = TextStyles.body,
                            color = TextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                Text(
                    text = stringResource(R.string.settings_delete_account_confirm_action),
                    color = DestructiveRed
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun DeleteAccountErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_delete_account_failed_title),
                style = TextStyles.titleMedium,
                color = TextDark
            )
        },
        text = {
            Text(
                text = message,
                style = TextStyles.body,
                color = TextMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_ok))
            }
        }
    )
}

private data class SettingsRowData(
    val title: String,
    val value: String = "",
    val showChevron: Boolean = false,
    val isDestructive: Boolean = false
)

private fun normalizedVersion(version: String): String {
    val components = version.split(".")
    if (components.isEmpty() || components.any { it.isBlank() }) return version
    if (components.size >= 3) return version
    return (components + List(3 - components.size) { "0" }).joinToString(".")
}

private fun lastSyncText(context: Context, lastWaterSyncAt: Long?): String {
    val timestamp = lastWaterSyncAt ?: return context.getString(R.string.settings_last_sync_never)
    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return context.getString(R.string.settings_last_sync_at, formatter.format(Date(timestamp)))
}

private val PageBackground = Color(0xFFF2F2F7)
private val TextDark = Color(0xFF1F2633)
private val TextMuted = Color(0xFF6E6E73)
private val DividerColor = Color(0xFFE5E5EA)
private val ChevronColor = Color(0xFFC7C7CC)
private val AccentBlue = Color(0xFF1877F2)
private val DestructiveRed = Color(0xFFD70015)
