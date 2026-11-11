package com.dialysis.app.ui.home.tabs.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R
import com.dialysis.app.ui.components.TextStyles
import java.time.ZoneId

/** Read-only profile card of Settings > Account (no edit controls). */
@Composable
fun AccountProfileCard(
    state: AccountProfileState,
    onRetry: () -> Unit,
) {
    ProfileCardContainer {
        when (state) {
            AccountProfileState.Idle,
            AccountProfileState.Loading -> ProfileLoading()
            is AccountProfileState.Error -> ProfileMessage(
                message = stringResource(
                    if (state.isNoInternet) R.string.account_profile_no_internet else R.string.account_profile_load_failed
                ),
                onRetry = onRetry
            )
            AccountProfileState.SessionExpired -> ProfileMessage(
                message = stringResource(R.string.session_expired_message),
                onRetry = onRetry
            )
            is AccountProfileState.Loaded -> {
                val texts = profileTexts()
                val profile = remember(state.user, texts) {
                    AccountProfileFormatter.format(state.user, texts, ZoneId.systemDefault())
                }
                ProfileDetails(profile)
            }
        }
    }
}

@Composable
private fun profileTexts(): ProfileTexts = ProfileTexts(
    placeholder = stringResource(R.string.account_profile_placeholder),
    genderMale = stringResource(R.string.register_gender_male),
    genderFemale = stringResource(R.string.register_gender_female),
    genderOther = stringResource(R.string.account_profile_gender_other),
    ageFormat = stringResource(R.string.account_profile_age_format),
    mlPerDayFormat = stringResource(R.string.account_profile_ml_per_day_format),
    timesPerWeekFormat = stringResource(R.string.account_profile_times_per_week_format),
    weightFormat = stringResource(R.string.account_profile_weight_format),
)

@Composable
private fun ProfileCardContainer(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun ProfileLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = AccentPurple, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        Text(text = stringResource(R.string.account_profile_loading), style = TextStyles.body, color = TextMuted)
    }
}

@Composable
private fun ProfileMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = message, style = TextStyles.body, color = TextMuted, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.common_retry), color = AccentPurple)
        }
    }
}

@Composable
private fun ProfileDetails(profile: AccountProfileUi) {
    ProfileHeader(profile)
    ProfileDivider()
    ProfileSectionTitle(stringResource(R.string.account_profile_contact_section))
    ProfileRow(Icons.Outlined.Email, stringResource(R.string.account_profile_email), profile.email)
    ProfileRow(Icons.Outlined.Phone, stringResource(R.string.account_profile_phone), profile.phone)
    ProfileRow(Icons.Outlined.Person, stringResource(R.string.account_profile_gender), profile.gender)
    ProfileRow(Icons.Outlined.Cake, stringResource(R.string.account_profile_age), profile.age)
    ProfileRow(Icons.Outlined.CalendarToday, stringResource(R.string.account_profile_created_at), profile.createdAt)
    ProfileDivider()
    ProfileSectionTitle(stringResource(R.string.account_profile_kidney_section))
    ProfileRow(
        label = stringResource(R.string.account_profile_daily_water_target),
        value = profile.dailyWaterTarget,
        highlightValue = true
    )
    ProfileRow(label = stringResource(R.string.account_profile_initial_weight), value = profile.initialWeight)
    ProfileRow(label = stringResource(R.string.account_profile_dialysis_start_year), value = profile.dialysisStartYear)
    ProfileRow(label = stringResource(R.string.account_profile_dialysis_freq_week), value = profile.dialysisFreqWeek)
    ProfileRow(label = stringResource(R.string.account_profile_daily_urine), value = profile.dailyUrine)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ProfileHeader(profile: AccountProfileUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Lavender, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.avatarLetter,
                color = AccentPurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = profile.name, style = TextStyles.title, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = profile.username, style = TextStyles.body, color = TextMuted)
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextMuted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector? = null,
    label: String,
    value: String,
    highlightValue: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text = label, style = TextStyles.body, color = TextDark, modifier = Modifier.weight(1f))
        if (highlightValue) {
            Text(
                text = value,
                style = TextStyles.body,
                fontWeight = FontWeight.SemiBold,
                color = AccentPurple,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .background(Lavender, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        } else {
            Text(
                text = value,
                style = TextStyles.body,
                color = TextDark,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor)
    )
}

private val TextDark = Color(0xFF1F2633)
private val TextMuted = Color(0xFF6E6E73)
private val DividerColor = Color(0xFFE5E5EA)
private val AccentPurple = Color(0xFF6B4EFF)
private val Lavender = Color(0xFFEDE7FF)
