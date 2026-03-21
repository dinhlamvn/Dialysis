package com.dialysis.app.ui.weight

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.TextStyles

private val AccentBlue = Color(0xFF1877F2)
private val LightCard = Color(0xFFF1F3F6)
private val TextDark = Color(0xFF2A2D34)
private val TextMuted = Color(0xFF8890A1)
private val Green = Color(0xFF10BA6C)

@Composable
fun WeightScreen(showBottomNav: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { GoalHeader() }
            item { WeightInfoCards() }
            item { ChartTabBar() }
            item { MonthChartCard() }
            item { PromoCards() }
            item { TryButton() }
        }

        if (showBottomNav) {
            WeightBottomNav()
        }
    }
}

@Composable
private fun GoalHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            Text(text = "Goal: ", style = TextStyles.title, color = TextMuted)
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = "61 kg", style = TextStyles.titleMedium, color = AccentBlue)
        }
    }
}

@Composable
private fun WeightInfoCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallInfoCard(title = "Initial weight", value = "63 kg", highlight = false)
            SmallInfoCard(title = "Current weight", value = "63 kg", highlight = true)
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .height(212.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightCard)
                    .padding(16.dp)
            ) {
                Text(text = "Weight progress  >", style = TextStyles.title, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "0 kg", style = TextStyles.titleMedium, color = TextDark)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(120.dp, 80.dp)
                        .background(Color(0xFFFF4D18), RoundedCornerShape(16.dp))
                        .align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun SmallInfoCard(title: String, value: String, highlight: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightCard)
                .padding(16.dp)
        ) {
            Text(text = title, style = TextStyles.title, color = TextMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = TextStyles.titleMedium,
                color = if (highlight) AccentBlue else TextDark
            )
        }
    }
}

@Composable
private fun ChartTabBar() {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F2F5))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Month", style = TextStyles.titleMedium, color = TextDark)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Year", style = TextStyles.titleMedium, color = TextDark)
            }
        }
    }
}

@Composable
private fun MonthChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(16.dp)
        ) {
            Text(
                text = "March",
                color = TextMuted,
                style = TextStyles.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                val gridColor = Color(0xFFB9C0CC)
                val blueLine = Color(0xFF0E66E5)
                val targetLine = Color(0xFF11A668)

                val top = 24f
                val bottom = size.height - 20f
                val left = 20f
                val right = size.width - 10f

                val levels = 6
                for (i in 0..levels) {
                    val y = top + (bottom - top) * (i / levels.toFloat())
                    drawLine(
                        color = gridColor,
                        start = Offset(left, y),
                        end = Offset(right, y),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f), 0f)
                    )
                }

                drawLine(
                    color = blueLine,
                    start = Offset(left, top + 80f),
                    end = Offset(right - 120f, bottom - 60f),
                    strokeWidth = 8f
                )
                drawLine(
                    color = targetLine,
                    start = Offset(left, bottom - 24f),
                    end = Offset(right, bottom - 24f),
                    strokeWidth = 5f
                )
            }
        }
    }
}

@Composable
private fun PromoCards() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Track your weight dynamics and enhance your\nwell-being with My Water",
                style = TextStyles.titleMedium,
                color = TextDark,
                modifier = Modifier.padding(18.dp),
                textAlign = TextAlign.Center
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PromoPill("Weight tracking", AccentBlue, Modifier.weight(1f))
            PromoPill("Well-being", Green, Modifier.weight(1f))
        }
        PromoPill("Healthy body", Color(0xFFF1A92A), Modifier.fillMaxWidth(0.5f).align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun PromoPill(text: String, color: Color, modifier: Modifier) {
    Card(shape = RoundedCornerShape(30.dp), modifier = modifier) {
        Text(
            text = text,
            color = color,
            style = TextStyles.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun TryButton() {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text("Try 3 days for free", style = TextStyles.titleMedium, color = Color.White)
    }
}

@Composable
private fun WeightBottomNav() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                label = "My Water",
                active = false,
                onClick = { context.startActivity(Router.home(context)) }
            )
            BottomItem(label = "Weight", active = true, onClick = {})
            FloatingAddButton()
            BottomItem(label = "Statistics", active = false, onClick = {})
            BottomItem(label = "Settings", active = false, onClick = {})
        }
    }
}

@Composable
private fun BottomItem(label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (active) AccentBlue else Color(0xFFBCC2CC), CircleShape)
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
