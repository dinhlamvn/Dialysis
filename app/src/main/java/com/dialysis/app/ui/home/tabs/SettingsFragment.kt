package com.dialysis.app.ui.home.tabs

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.config.AppGoals
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.theme.AppTheme

class SettingsFragment : BaseFragment() {
    @Composable
    override fun ContentView() {
        AppTheme {
            SettingsScreen()
        }
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = TextStyles.titleMedium,
                color = Color(0xFF1F2633),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { TopCardsSection() }
        item { FullVersionBanner() }
        item { SettingsGroupOne() }
        item { SettingsGroupTwo() }
        item { SocialSection() }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun TopCardsSection() {
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
                    Text("Account", style = TextStyles.titleMedium, color = Color(0xFF2A2F39))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sign In or Sign Up", style = TextStyles.titleMedium, color = Color(0xFF1877F2))
                }
                Box(
                    modifier = Modifier
                        .size(width = 130.dp, height = 100.dp)
                        .background(Color(0xFFC8D7E6), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Profile", style = TextStyles.title, color = Color(0xFF2A2F39))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = "Daily goal",
                value = "${AppGoals.DAILY_WATER_GOAL_ML} ml",
                bg = Color(0xFFE9EDF2),
                valueColor = Color(0xFF17A9DC)
            )
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = "Notifications",
                value = "Off",
                bg = Color(0xFFFF4D57),
                valueColor = Color.White,
                titleColor = Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = "Friends",
                value = "Add",
                bg = Color(0xFF9C53DF),
                valueColor = Color.White,
                titleColor = Color.White
            )
            SmallColorCard(
                modifier = Modifier.weight(1f),
                title = "Achievements",
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
                Text("Full version ›", style = TextStyles.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Drink more water and use\nthe app effectively",
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
            Triple("Beverages", "", true),
            Triple("Default volumes", "", true),
            Triple("App style", "", true),
            Triple("Home screen character", "", true),
            Triple("Language", "English", true),
            Triple("Integrations", "", true),
            Triple("Units of measure", "Metric", true)
        )
    )
}

@Composable
private fun SettingsGroupTwo() {
    SettingsGroupCard(
        rows = listOf(
            Triple("Rate the app", "", true),
            Triple("Suggest feature", "", true),
            Triple("Contact us", "", true),
            Triple("Share app", "", true)
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
        Text("Join our community", style = TextStyles.title, color = Color(0xFFA7AEBB))
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
