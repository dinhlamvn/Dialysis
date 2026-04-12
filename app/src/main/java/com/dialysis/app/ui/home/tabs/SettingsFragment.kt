package com.dialysis.app.ui.home.tabs

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.R
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.login.LoginActivity
import com.dialysis.app.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    val isLoadingAccount = viewModel.isLoadingAccountState.collectAsStateWithLifecycle().value
    val accountContact = viewModel.accountContactState.collectAsStateWithLifecycle().value
    val isLoggedIn = viewModel.isLoggedInState.collectAsStateWithLifecycle().value
    val dailyGoalMl = viewModel.dailyGoalMlState.collectAsStateWithLifecycle().value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = TextStyles.titleMedium,
                color = Color(0xFF1F2633),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TopCardsSection(
                accountContact = accountContact,
                isLoadingAccount = isLoadingAccount,
                isLoggedIn = isLoggedIn,
                dailyGoalMl = dailyGoalMl
            )
        }
        item { FullVersionBanner() }
        item { SettingsGroupOne() }
        item { SettingsGroupTwo() }
        item { SocialSection() }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun TopCardsSection(
    accountContact: String?,
    isLoadingAccount: Boolean,
    isLoggedIn: Boolean,
    dailyGoalMl: Int
) {
    val context = LocalContext.current
    val accountText = accountContact ?: stringResource(R.string.settings_sign_in_up)
    val isGuestState = accountContact == null

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8ECF1))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.settings_account),
                        style = TextStyles.titleMedium,
                        color = Color(0xFF2A2F39)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoadingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF1877F2),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = accountText,
                            style = TextStyles.titleMedium,
                            color = if (isGuestState) Color(0xFF1877F2) else Color(0xFF2A2F39),
                            modifier = if (isGuestState) {
                                Modifier.clickable {
                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                }
                            } else {
                                Modifier
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(width = 130.dp, height = 100.dp)
                        .background(Color(0xFFC8D7E6), RoundedCornerShape(16.dp))
                        .clickable {
                            if (isLoggedIn) {
                                context.startActivity(Router.info(context))
                            } else {
                                context.startActivity(Intent(context, LoginActivity::class.java))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.settings_profile),
                        style = TextStyles.title,
                        color = Color(0xFF2A2F39)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_daily_goal),
                value = "$dailyGoalMl ml",
                bg = Color(0xFFE9EDF2),
                valueColor = Color(0xFF17A9DC)
            )
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_notifications),
                value = stringResource(R.string.settings_off),
                bg = Color(0xFFFF4D57),
                valueColor = Color.White,
                titleColor = Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_friends),
                value = stringResource(R.string.settings_add),
                bg = Color(0xFF9C53DF),
                valueColor = Color.White,
                titleColor = Color.White
            )
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_achievements),
                value = "0 / 13",
                bg = Color(0xFFE8ECF1),
                valueColor = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun SmallColorCard(
    modifier: Modifier,
    title: String,
    value: String,
    bg: Color,
    valueColor: Color,
    titleColor: Color = Color(0xFF2A2F39)
) {
    Card(shape = RoundedCornerShape(22.dp), modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = TextStyles.titleMedium, color = titleColor)
                Text("›", style = TextStyles.titleMedium, color = titleColor.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = TextStyles.titleMedium, color = valueColor)
        }
    }
}

@Composable
private fun FullVersionBanner() {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF5FCBEE), Color(0xFF2C95EC))))
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_full_version),
                    style = TextStyles.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.settings_full_version_desc),
                    style = TextStyles.title,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFF1494E8), CircleShape)
            )
        }
    }
}

@Composable
private fun SettingsGroupOne() {
    SettingsGroupCard(
        rows = listOf(
            Triple(stringResource(R.string.settings_beverages), "", true),
            Triple(stringResource(R.string.settings_default_volumes), "", true),
            Triple(stringResource(R.string.settings_app_style), "", true),
            Triple(stringResource(R.string.settings_home_character), "", true),
            Triple(stringResource(R.string.settings_language), stringResource(R.string.settings_language_value), true),
            Triple(stringResource(R.string.settings_integrations), "", true),
            Triple(stringResource(R.string.settings_units), stringResource(R.string.settings_units_metric), true)
        )
    )
}

@Composable
private fun SettingsGroupTwo() {
    SettingsGroupCard(
        rows = listOf(
            Triple(stringResource(R.string.settings_rate_app), "", true),
            Triple(stringResource(R.string.settings_suggest_feature), "", true),
            Triple(stringResource(R.string.settings_contact_us), "", true),
            Triple(stringResource(R.string.settings_share_app), "", true)
        )
    )
}

@Composable
private fun SettingsGroupCard(rows: List<Triple<String, String, Boolean>>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8ECF1))
                .padding(10.dp)
        ) {
            rows.forEachIndexed { index, row ->
                Card(shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row.first, style = TextStyles.titleMedium, color = Color(0xFF2A2F39))
                        Spacer(modifier = Modifier.weight(1f))
                        if (row.second.isNotBlank()) {
                            Text(row.second, style = TextStyles.title, color = Color(0xFF7A8498))
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text("›", style = TextStyles.titleMedium, color = Color(0xFFA7AEBB))
                    }
                }
                if (index != rows.lastIndex) {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}

@Composable
private fun SocialSection() {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.settings_join_community),
            style = TextStyles.title,
            color = Color(0xFFA7AEBB)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
            SocialCircle("f", Color(0xFF1877F2))
            SocialCircle("ig", Color(0xFFE1306C))
            SocialCircle("x", Color(0xFF1DA1F2))
        }
    }
}

@Composable
private fun SocialCircle(text: String, bg: Color) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = TextStyles.titleMedium, color = Color.White)
    }
}
