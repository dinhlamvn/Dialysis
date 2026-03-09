package com.dialysis.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.R
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.TextStyles

private val BlueTop = Color(0xFF2D6FDD)
private val BlueBottom = Color(0xFF1A50C9)
private val CardLight = Color(0xFFF3F3F7)
private val CardGray = Color(0xFFBFC3CB)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val AccentBlue = Color(0xFF1877F2)

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HeaderCard()

        Spacer(modifier = Modifier.height(16.dp))

        DrinksSection()

        Spacer(modifier = Modifier.height(16.dp))

        WeeklySection()

        Spacer(modifier = Modifier.weight(1f))

        BottomNav()
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BlueTop, BlueBottom)))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    color = Color.White,
                    style = TextStyles.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    WaterCircle()
                    Column {
                        Text(
                            text = stringResource(R.string.home_progress_value),
                            color = Color.White,
                            style = TextStyles.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.home_progress_detail),
                            color = Color.White.copy(alpha = 0.8f),
                            style = TextStyles.body
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterCircle() {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun PersonFigure() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(screenHeight * 0.3f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val bodyColor = Color(0xFF2F6BDB)
            val headColor = Color(0xFF4D84EC)
            val legColor = Color.White

            // Head
            drawCircle(
                color = headColor,
                radius = w * 0.095f,
                center = Offset(w * 0.5f, h * 0.14f)
            )

            // Arms
            drawLine(
                color = bodyColor,
                start = Offset(w * 0.20f, h * 0.33f),
                end = Offset(w * 0.39f, h * 0.24f),
                strokeWidth = w * 0.10f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bodyColor,
                start = Offset(w * 0.61f, h * 0.24f),
                end = Offset(w * 0.80f, h * 0.33f),
                strokeWidth = w * 0.10f,
                cap = StrokeCap.Round
            )

            // Body
            val bodyPath = Path().apply {
                moveTo(w * 0.36f, h * 0.24f)
                lineTo(w * 0.64f, h * 0.24f)
                lineTo(w * 0.64f, h * 0.68f)
                lineTo(w * 0.36f, h * 0.68f)
                close()
            }
            drawPath(
                path = bodyPath,
                color = bodyColor,
                style = Fill
            )

            drawLine(
                color = bodyColor,
                start = Offset(w * 0.36f, h * 0.28f),
                end = Offset(w * 0.36f, h * 0.64f),
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bodyColor,
                start = Offset(w * 0.64f, h * 0.28f),
                end = Offset(w * 0.64f, h * 0.64f),
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bodyColor,
                start = Offset(w * 0.42f, h * 0.24f),
                end = Offset(w * 0.58f, h * 0.24f),
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round
            )

            // Fixed legs
            drawLine(
                color = legColor,
                start = Offset(w * 0.46f, h * 0.70f),
                end = Offset(w * 0.33f, h * 0.96f),
                strokeWidth = w * 0.11f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = legColor,
                start = Offset(w * 0.54f, h * 0.70f),
                end = Offset(w * 0.67f, h * 0.96f),
                strokeWidth = w * 0.11f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun DrinksSection() {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_drinks_title),
                color = TextDark,
                style = TextStyles.titleMedium
            )
            Text(
                text = stringResource(R.string.home_edit),
                color = AccentBlue,
                style = TextStyles.bodyMedium,
                modifier = Modifier.clickable {
                    context.startActivity(Router.dailyReport(context))
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            item {
                SmallDrinkCard(
                    title = "+",
                    subtitle = stringResource(R.string.home_add)
                )
            }
            item {
                DrinkCard(
                    amount = stringResource(R.string.home_smoothie_amount),
                    name = stringResource(R.string.home_smoothie),
                    time = stringResource(R.string.home_time)
                )
            }
            item {
                DrinkCard(
                    amount = stringResource(R.string.home_tea_amount),
                    name = stringResource(R.string.home_tea),
                    time = stringResource(R.string.home_time)
                )
            }
        }
    }
}

@Composable
private fun SmallDrinkCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .size(width = 110.dp, height = 130.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardLight),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = AccentBlue,
                    style = TextStyles.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    style = TextStyles.caption
                )
            }
        }
    }
}

@Composable
private fun DrinkCard(amount: String, name: String, time: String) {
    Card(
        modifier = Modifier
            .size(width = 130.dp, height = 130.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardLight)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardGray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                color = AccentBlue,
                style = TextStyles.bodyMedium
            )
            Text(
                text = name,
                color = TextDark,
                style = TextStyles.body
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                color = TextMuted,
                style = TextStyles.caption
            )
        }
    }
}

@Composable
private fun WeeklySection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_weekly_title),
                color = TextDark,
                style = TextStyles.titleMedium
            )
            Text(
                text = stringResource(R.string.home_more),
                color = AccentBlue,
                style = TextStyles.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(BlueTop, BlueBottom)))
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val days = listOf("Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN", "Th 2")
                        days.forEach { day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = Color.White,
                                        style = TextStyles.caption
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "0 ml",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = TextStyles.caption
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_weekly_avg),
                                color = Color.White.copy(alpha = 0.8f),
                                style = TextStyles.body
                            )
                            Text(
                                text = stringResource(R.string.home_weekly_percent),
                                color = Color.White,
                                style = TextStyles.titleMedium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.home_weekly_total),
                                color = Color.White.copy(alpha = 0.8f),
                                style = TextStyles.body
                            )
                            Text(
                                text = stringResource(R.string.home_weekly_total_value),
                                color = Color.White,
                                style = TextStyles.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNav() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(label = stringResource(R.string.home_nav_water), active = true)
            BottomItem(label = stringResource(R.string.home_nav_weight), active = false)
            FloatingAddButton()
            BottomItem(label = stringResource(R.string.home_nav_stats), active = false)
            BottomItem(label = stringResource(R.string.home_nav_settings), active = false)
        }
    }
}

@Composable
private fun BottomItem(label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) AccentBlue else CardGray)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (active) AccentBlue else TextMuted,
            style = TextStyles.caption
        )
    }
}

@Composable
private fun FloatingAddButton() {
    Button(
        onClick = {},
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "+",
            color = Color.White,
            style = TextStyles.titleMedium
        )
    }
}
