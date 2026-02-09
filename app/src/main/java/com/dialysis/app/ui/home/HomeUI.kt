package com.dialysis.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R
import com.dialysis.app.router.Router

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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
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
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.home_progress_detail),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_edit),
                color = AccentBlue,
                fontSize = 14.sp,
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
                    fontSize = 28.sp,
                    color = AccentBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
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
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = name,
                color = TextDark,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                color = TextMuted,
                fontSize = 11.sp
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_more),
                color = AccentBlue,
                fontSize = 14.sp
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
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "0 ml",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
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
                                fontSize = 12.sp
                            )
                            Text(
                                text = stringResource(R.string.home_weekly_percent),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.home_weekly_total),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = stringResource(R.string.home_weekly_total_value),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            fontSize = 11.sp
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
            fontSize = 24.sp
        )
    }
}
