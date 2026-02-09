package com.dialysis.app.ui.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.PrimaryButton

private val PageBackground = Color(0xFFFFFFFF)
private val AccentBlue = Color(0xFF1877F2)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val CardSurface = Color(0xFFF8F8FB)
private val IconTint = Color(0xFFF2B04A)

@Composable
fun DailyReportScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopBar()

        Spacer(modifier = Modifier.height(20.dp))

        ProgressBar(progress = 0.24f)

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DrinkItem(
                title = stringResource(R.string.daily_coconut_title),
                subtitle = stringResource(R.string.daily_coconut_subtitle),
                time = stringResource(R.string.daily_time)
            )
            DrinkItem(
                title = stringResource(R.string.daily_coffee_title),
                subtitle = stringResource(R.string.daily_coffee_subtitle),
                time = stringResource(R.string.daily_time)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = stringResource(R.string.daily_add_drink),
            onClick = { context.startActivity(Router.drinkList(context)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F2F7)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = TextDark,
                fontSize = 20.sp
            )
        }

        Text(
            text = stringResource(R.string.daily_date),
            color = TextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(40.dp))
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE6E6EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(AccentBlue)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.daily_progress),
            color = TextDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DrinkItem(title: String, subtitle: String, time: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(CardSurface)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE9F1FF)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(IconTint)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            Text(
                text = time,
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF4E3)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🗑",
                    fontSize = 14.sp
                )
            }
        }
    }
}
