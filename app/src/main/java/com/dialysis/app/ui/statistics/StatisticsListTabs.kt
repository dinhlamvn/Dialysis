package com.dialysis.app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsByDayTab(dailyStats: List<DailyStatUi>, onDayClick: (Long) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (dailyStats.isEmpty()) {
            Text("Chưa có bản ghi theo ngày", color = Color(0xFF7A8498), modifier = Modifier.padding(vertical = 40.dp))
        } else {
            dailyStats.forEach { stat ->
                ProgressRow(
                    title = stat.listTitle,
                    totalMl = stat.totalMl,
                    percentage = stat.percentage,
                    titleWidth = 100.dp
                ) { onDayClick(stat.dateMillis) }
            }
        }
    }
}

@Composable
fun StatisticsMonthlyTab(summaries: List<MonthSummaryUi>, onMonthClick: (Long) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        summaries.forEach { summary ->
            ProgressRow(
                title = summary.title,
                totalMl = summary.totalMl,
                percentage = summary.averagePercentage,
                titleWidth = 120.dp
            ) { onMonthClick(summary.monthStartMillis) }
        }
    }
}

@Composable
private fun ProgressRow(
    title: String,
    totalMl: Int,
    percentage: Int,
    titleWidth: Dp,
    onClick: () -> Unit
) {
    val rowShape = RoundedCornerShape(26.dp)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(4.dp, rowShape)
            .clip(rowShape)
            .background(Color(0xFFE6E9EF))
            .border(1.dp, Color(0xFFD1D5DB), rowShape)
            .clickable(onClick = onClick)
    ) {
        val clampedPercentage = percentage.coerceIn(0, 100)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(maxWidth * (clampedPercentage / 100f))
                .background(Color(0xFF1877F2))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(titleWidth)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                if (percentage > 0) "$percentage% - ${formatMl(totalMl)}" else "0%",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text("›", color = Color(0xFFD1D5DB), fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatisticsMonthDetail(
    monthStartMillis: Long,
    monthDays: List<MonthDayUi>,
    onDayClick: (Long) -> Unit
) {
    val total = monthDays.sumOf { it.totalMl }
    val avg = if (monthDays.isEmpty()) 0 else total / monthDays.size
    val avgPct = if (monthDays.isEmpty()) 0 else monthDays.map { it.percentage }.average().toInt()
    val daysOnGoal = monthDays.count { it.percentage >= 100 }
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(monthYearTitle(monthStartMillis), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF2F3F6), RoundedCornerShape(12.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryItem("Tổng", formatMl(total))
            SummaryItem("Trung bình", "${formatMl(avg)} ($avgPct%)")
            SummaryItem("Đạt mục tiêu", "$daysOnGoal/${monthDays.size}")
        }
        Text("Lịch", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        CalendarGrid(days = monthDays, onDayClick = onDayClick)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryItem(title: String, value: String) {
    Column {
        Text(title, color = Color(0xFF6B7280), fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun CalendarGrid(days: List<MonthDayUi>, onDayClick: (Long) -> Unit) {
    val headers = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) { headers.forEach { Text(it, color = Color(0xFF6B7280), fontSize = 12.sp, modifier = Modifier.weight(1f)) } }
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        DayRing(day, Modifier.clickable { onDayClick(day.dateMillis) })
                    }
                }
                repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayRing(day: MonthDayUi, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(Color(0xFFE5E7EB), style = Stroke(width = 2.dp.toPx()))
            if (day.percentage > 0) {
                drawArc(Color(0xFF1877F2), -90f, day.percentage * 3.6f, false, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        Text(day.day.toString(), fontSize = 14.sp, fontWeight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal)
    }
}
