package com.dialysis.app.ui.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.theme.AppTheme

class StatisticsFragment : BaseFragment() {
    @Composable
    override fun ContentView() {
        AppTheme {
            StatisticsScreen()
        }
    }
}

@Composable
fun StatisticsScreen() {
    StatisticsScreen(
        todayTotalMl = 0,
        weekTotalMl = 0,
        monthTotalMl = 0,
        weekDailyMl = listOf(0, 0, 0, 0, 0, 0, 0)
    )
}

@Composable
fun StatisticsScreen(
    todayTotalMl: Int,
    weekTotalMl: Int,
    monthTotalMl: Int,
    weekDailyMl: List<Int>
) {
    var period by remember { mutableStateOf(StatsPeriod.WEEK) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Statistics",
                style = TextStyles.titleMedium,
                color = Color(0xFF1F2633),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabChip(text = "Main stat", selected = true)
                TabChip(text = "By day", selected = false)
                TabChip(text = "Beverages", selected = false)
                TabChip(text = "Monthly", selected = false)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PeriodChip(
                    text = "Day",
                    selected = period == StatsPeriod.DAY,
                    onClick = { period = StatsPeriod.DAY }
                )
                PeriodChip(
                    text = "Week",
                    selected = period == StatsPeriod.WEEK,
                    onClick = { period = StatsPeriod.WEEK }
                )
                PeriodChip(
                    text = "Month",
                    selected = period == StatsPeriod.MONTH,
                    onClick = { period = StatsPeriod.MONTH }
                )
            }
        }

        item {
            SummaryCard(
                period = period,
                todayTotalMl = todayTotalMl,
                weekTotalMl = weekTotalMl,
                monthTotalMl = monthTotalMl
            )
        }

        item {
            TodaySection(
                weekDailyMl = weekDailyMl,
                weekTotalMl = weekTotalMl
            )
        }
        item { YesterdaySection() }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun TodaySection(
    weekDailyMl: List<Int>,
    weekTotalMl: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Today, 20 March", style = TextStyles.titleMedium, color = Color(0xFF1F2633))
            Text(text = "Find friends", style = TextStyles.title, color = Color(0xFF1877F2))
        }
        FriendCard()
        Text(
            text = "Statistics for the week",
            style = TextStyles.titleMedium,
            color = Color(0xFF1F2633),
            modifier = Modifier.padding(top = 8.dp)
        )
        WeeklyStatCard(
            weekDailyMl = weekDailyMl,
            weekTotalMl = weekTotalMl
        )
    }
}

@Composable
private fun YesterdaySection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Yesterday, 19 March", style = TextStyles.titleMedium, color = Color(0xFF1F2633))
        FriendCard()
    }
}

@Composable
private fun FriendCard() {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEFF3))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FB))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFE3E7EF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👤", style = TextStyles.title)
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(text = "Me", style = TextStyles.titleMedium, color = Color(0xFF29303D))
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "8%", style = TextStyles.title, color = Color(0xFF7E8798))
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(text = "›", style = TextStyles.titleMedium, color = Color(0xFF1877F2))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFFE6EAF2), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.08f)
                                .height(4.dp)
                                .background(Color(0xFF1DA6F8), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    text = "👥  Invite friends",
                    style = TextStyles.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFF37B5F8))
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun WeeklyStatCard(
    weekDailyMl: List<Int>,
    weekTotalMl: Int
) {
    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEFF3))
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1691EE), RoundedCornerShape(24.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                val safeValues = if (weekDailyMl.size == 7) weekDailyMl else listOf(0, 0, 0, 0, 0, 0, 0)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .border(2.dp, Color(0xFF53B1F6), CircleShape)
                                    .background(Color(0x00000000), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = day, color = Color.White, style = TextStyles.title)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatMlToLitresText(safeValues[index]),
                                color = Color.White,
                                style = TextStyles.titleMedium
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Average daily percentage", color = Color.White, style = TextStyles.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (weekTotalMl > 0) "100%" else "0%",
                            color = Color.White,
                            style = TextStyles.titleMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total for the week", color = Color.White, style = TextStyles.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatMlToLitresText(weekTotalMl),
                            color = Color.White,
                            style = TextStyles.titleMedium
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "☕", style = TextStyles.titleMedium)
                Text(text = "200 ml", color = Color(0xFF1877F2), style = TextStyles.title)
                Text(text = "Coffee", color = Color(0xFF7E8798), style = TextStyles.titleMedium)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    period: StatsPeriod,
    todayTotalMl: Int,
    weekTotalMl: Int,
    monthTotalMl: Int
) {
    val value = when (period) {
        StatsPeriod.DAY -> formatMlText(todayTotalMl)
        StatsPeriod.WEEK -> formatMlText(weekTotalMl)
        StatsPeriod.MONTH -> formatMlText(monthTotalMl)
    }
    val title = when (period) {
        StatsPeriod.DAY -> "Daily tracking"
        StatsPeriod.WEEK -> "Weekly tracking"
        StatsPeriod.MONTH -> "Monthly tracking"
    }
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEFF3))
                .padding(14.dp)
        ) {
            Text(text = title, style = TextStyles.bodyMedium, color = Color(0xFF5B6475))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = TextStyles.titleMedium, color = Color(0xFF1877F2))
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF7A8498),
            style = TextStyles.bodyMedium,
            modifier = Modifier
                .background(if (selected) Color(0xFF1877F2) else Color(0xFFEDEFF3))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun TabChip(text: String, selected: Boolean) {
    if (selected) {
        Card(
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                style = TextStyles.titleMedium,
                modifier = Modifier
                    .background(Color(0xFF1877F2))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    } else {
        Text(
            text = text,
            color = Color(0xFF7A8498),
            style = TextStyles.titleMedium
        )
    }
}

private enum class StatsPeriod {
    DAY, WEEK, MONTH
}

private fun formatMlText(valueMl: Int): String {
    return if (valueMl >= 1000) {
        val litres = valueMl / 1000f
        String.format("%.1f l", litres)
    } else {
        "$valueMl ml"
    }
}

private fun formatMlToLitresText(valueMl: Int): String {
    val litres = valueMl / 1000f
    return if (litres == 0f) "0 l" else String.format("%.1f l", litres)
}
