package com.dialysis.app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsMainTab(
    state: StatisticsUiState,
    onDayClick: (Long) -> Unit,
    onMonthClick: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TodayProgressCard(state = state, onClick = { onDayClick(System.currentTimeMillis()) })
        Text("Thống kê trong tuần", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        WeeklyStatsCard(weeklyStats = state.weeklyStats, onDayClick = onDayClick)
        WeeklyBeverageStrip(stats = state.weeklyBeverageStats)
        CurrentMonthCard(days = state.currentMonthDays, onDayClick = onDayClick, onMonthClick = onMonthClick)
    }
}

@Composable
private fun TodayProgressCard(state: StatisticsUiState, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Hôm nay", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAF4FF), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("👤", fontSize = 16.sp)
            Text("Tôi", fontSize = 14.sp)
            SegmentedProgress(percentage = state.todayPercentage, modifier = Modifier.weight(1f))
            Text("${state.todayPercentage}%", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Text("›", color = Color.Gray, fontSize = 18.sp)
        }
    }
}

@Composable
private fun WeeklyStatsCard(weeklyStats: WeeklyStatsUi, onDayClick: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF4DA3FF))), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            weeklyStats.dailyStats.forEach { stat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { onDayClick(stat.dateMillis) }) {
                    PercentRing(stat.percentage, stat.label, Color.White, Color.White.copy(alpha = 0.3f), Modifier.size(38.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatMl(stat.totalMl), color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp, maxLines = 1)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Phần trăm trung bình hằng ngày", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(String.format("%.1f%%", weeklyStats.averagePercentage), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Tổng số trong tuần", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(formatMl(weeklyStats.weeklyTotalMl), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WeeklyBeverageStrip(stats: List<BeverageStatUi>) {
    val consumed = stats.filter { it.volumeMl > 0 }
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF2F3F6), RoundedCornerShape(16.dp)).horizontalScroll(rememberScrollState()).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (consumed.isEmpty()) Text("7 ngày qua", color = Color(0xFF7A8498), fontSize = 13.sp)
        consumed.forEach { stat ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                Text(stat.visual.icon, fontSize = 28.sp)
                Text("${formatMl(stat.volumeMl)} ${stat.visual.title}", fontSize = 9.sp, maxLines = 2)
            }
        }
    }
}

@Composable
private fun CurrentMonthCard(days: List<MonthDayUi>, onDayClick: (Long) -> Unit, onMonthClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tháng", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Thêm nữa", color = Color(0xFF1877F2), fontSize = 14.sp, modifier = Modifier.clickable { days.firstOrNull()?.let { onMonthClick(it.dateMillis) } })
        }
        CalendarGrid(days = days, onDayClick = onDayClick)
    }
}

@Composable
fun PercentRing(percentage: Int, text: String, color: Color, trackColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(trackColor, style = Stroke(width = 3.dp.toPx()))
            drawArc(color, -90f, percentage.coerceIn(0, 100) * 3.6f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SegmentedProgress(percentage: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(8.dp).background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(4.dp))) {
        Row(modifier = Modifier.fillMaxWidth(percentage / 100f).height(8.dp)) {
            Box(Modifier.weight(1f).height(8.dp).background(Color(0xFF8B5A3C), RoundedCornerShape(4.dp)))
            Box(Modifier.weight(1f).height(8.dp).background(Color(0xFFF4C542)))
            Box(Modifier.weight(1f).height(8.dp).background(Color(0xFF1877F2), RoundedCornerShape(4.dp)))
        }
    }
}
