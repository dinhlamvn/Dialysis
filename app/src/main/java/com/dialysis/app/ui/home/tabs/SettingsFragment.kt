package com.dialysis.app.ui.home.tabs

import android.content.Context
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val isLoadingAccount = viewModel.isLoadingAccountState.collectAsStateWithLifecycle().value
    val accountContact = viewModel.accountContactState.collectAsStateWithLifecycle().value
    val isLoggedIn = viewModel.isLoggedInState.collectAsStateWithLifecycle().value
    val lastWaterSyncAt = viewModel.lastWaterSyncAtState.collectAsStateWithLifecycle().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = TextStyles.titleMedium,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                )
            }

            item {
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

            item {
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

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsSectionHeader(stringResource(R.string.settings_preferences))
                    SettingsCard {
                        if (isLoadingAccount) {
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
                        } else if (isLoggedIn) {
                            SettingsRow(
                                data = SettingsRowData(
                                    title = stringResource(R.string.settings_sign_out),
                                    value = accountContact.orEmpty(),
                                    isDestructive = true
                                ),
                                onClick = viewModel::signOut
                            )
                        } else {
                            SettingsRow(
                                data = SettingsRowData(
                                    title = stringResource(R.string.settings_sign_in),
                                    showChevron = true
                                ),
                                onClick = { context.startActivity(Router.login(context)) }
                            )
                        }
                    }
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
