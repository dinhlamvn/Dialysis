package com.dialysis.app.ui.home.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.base.BaseFragment
import com.dialysis.app.config.AppGoals
import com.dialysis.app.data.local.model.DailyTotal
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.daily.DailyReportScreen
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.theme.AppTheme
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private const val DailyGoalMl = AppGoals.DAILY_WATER_GOAL_ML

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
        weekDailyMl = listOf(0, 0, 0, 0, 0, 0, 0),
        dailyTotals = emptyList(),
        dailyReportViewModel = null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    todayTotalMl: Int,
    weekTotalMl: Int,
    monthTotalMl: Int,
    weekDailyMl: List<Int>,
    dailyTotals: List<DailyTotal>,
    dailyReportViewModel: DailyReportViewModel?
) {
    var activeTab by rememberSaveable { mutableStateOf(StatsTab.BY_DAY) }
    var selectedReportDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatsTab.entries.forEach { tab ->
                    TabChip(
                        text = tab.title,
                        selected = activeTab == tab,
                        onClick = { activeTab = tab }
                    )
                }
            }
        }

        item {
            when (activeTab) {
                StatsTab.MAIN -> MainStatSection(
                    todayTotalMl = todayTotalMl,
                    weekTotalMl = weekTotalMl,
                    weekDailyMl = weekDailyMl
                )
                StatsTab.BY_DAY -> ByDayReportSection(
                    dailyTotals = dailyTotals,
                    onRowClick = { day ->
                        val dateMillis = parseDayToCalendar(day)?.timeInMillis ?: return@ByDayReportSection
                        dailyReportViewModel?.showDateReport(dateMillis)
                        selectedReportDateMillis = dateMillis
                    }
                )
                StatsTab.BEVERAGES -> LockedStatsSection()
                StatsTab.MONTHLY -> LockedStatsSection()
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (selectedReportDateMillis != null && dailyReportViewModel != null) {
        ModalBottomSheet(
            sheetState = reportSheetState,
            onDismissRequest = {
                selectedReportDateMillis = null
                dailyReportViewModel.showTodayReport()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
            ) {
                DailyReportScreen(viewModel = dailyReportViewModel)
            }
        }
    }
}

@Composable
private fun MainStatSection(
    todayTotalMl: Int,
    weekTotalMl: Int,
    weekDailyMl: List<Int>
) {
    val currentDateLabel = remember { formatDayMonth(Calendar.getInstance()) }
    val safeWeekValues = if (weekDailyMl.size == 7) weekDailyMl else List(7) { 0 }
    val weekAveragePercent = ((weekTotalMl / 7f) / DailyGoalMl * 100).roundToInt().coerceAtLeast(0)
    val todayPercent = ((todayTotalMl / DailyGoalMl.toFloat()) * 100).roundToInt().coerceIn(0, 100)

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today, $currentDateLabel",
                style = TextStyles.titleMedium,
                color = Color(0xFF1F2633)
            )
            Text(
                text = "Find friends",
                style = TextStyles.title,
                color = Color(0xFF1877F2)
            )
        }

        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDEFF3))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFDDE4F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Me", style = TextStyles.bodyMedium, color = Color(0xFF6A7387))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Me", style = TextStyles.titleMedium, color = Color(0xFF1F2633))
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "$todayPercent%", style = TextStyles.title, color = Color(0xFF7A8498))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = ">", style = TextStyles.titleMedium, color = Color(0xFF1877F2))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color(0xFFE6EAF2), RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((todayTotalMl / DailyGoalMl.toFloat()).coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .background(Color(0xFF32B5F7), RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Invite friends",
                        color = Color.White,
                        style = TextStyles.titleMedium,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Text(
            text = "Statistics for the week",
            style = TextStyles.titleMedium,
            color = Color(0xFF1F2633)
        )

        WeeklyStatCard(
            weekDailyMl = safeWeekValues,
            weekTotalMl = weekTotalMl,
            averagePercentage = weekAveragePercent
        )
    }
}

@Composable
private fun WeeklyStatCard(
    weekDailyMl: List<Int>,
    weekTotalMl: Int,
    averagePercentage: Int
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val todayIndex = ((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7)

    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEFF3))
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1691EE), RoundedCornerShape(28.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        if (index == todayIndex) Color.White.copy(alpha = 0.16f) else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = day, color = Color.White, style = TextStyles.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatMlToLitresText(weekDailyMl[index]),
                                color = Color.White,
                                style = TextStyles.title
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Average daily percentage", color = Color.White, style = TextStyles.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$averagePercentage%", color = Color.White, style = TextStyles.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total for the week", color = Color.White, style = TextStyles.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = formatMlToLitresText(weekTotalMl), color = Color.White, style = TextStyles.titleMedium)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 52.dp)
                        .background(Color(0xFF1AAAF4), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "200 ml", color = Color(0xFF1877F2), style = TextStyles.title)
                Text(text = "Water", color = Color(0xFF7A8498), style = TextStyles.titleMedium)
            }
        }
    }
}

@Composable
private fun ByDayReportSection(
    dailyTotals: List<DailyTotal>,
    onRowClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDEFF3))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (dailyTotals.isEmpty()) {
                    EmptyByDayState()
                } else {
                    dailyTotals.forEach { dailyTotal ->
                        DayReportRow(
                            title = formatGroupedDateTitle(dailyTotal.day),
                            totalMl = dailyTotal.totalMl,
                            onClick = { onRowClick(dailyTotal.day) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyByDayState() {
    Text(
        text = "No daily records yet",
        style = TextStyles.bodyMedium,
        color = Color(0xFF7A8498),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
    )
}

@Composable
private fun DayReportRow(
    title: String,
    totalMl: Int,
    onClick: () -> Unit
) {
    val progress = (totalMl / DailyGoalMl.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100).roundToInt()
    val minIndicatorWidth = 20.dp

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = TextStyles.titleMedium,
                color = Color(0xFF1F2633)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(Color(0xFFE0E7ED), RoundedCornerShape(20.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .widthIn(min = if (totalMl > 0) minIndicatorWidth else 0.dp)
                            .background(Color(0xFF35B7F7), RoundedCornerShape(20.dp))
                    )

                    Text(
                        text = "$percent% - ${formatMlText(totalMl)}",
                        style = TextStyles.titleMedium,
                        color = Color(0xFF1F2633),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 28.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Text(
                    text = ">",
                    style = TextStyles.titleMedium,
                    color = Color(0xFF1877F2)
                )
            }
        }
    }
}

@Composable
private fun LockedStatsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .background(Color(0xFF1BAAE5), CircleShape)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Details statistics are available in the full\napplication version only",
            color = Color(0xFF44505E),
            style = TextStyles.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {},
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1FB4EA))
        ) {
            Text(
                text = "More",
                color = Color.White,
                style = TextStyles.titleMedium,
                modifier = Modifier.padding(horizontal = 34.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.clickable(onClick = onClick)
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
            style = TextStyles.titleMedium,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}

private enum class StatsTab(val title: String) {
    MAIN("Main stat"),
    BY_DAY("By day"),
    BEVERAGES("Beverages"),
    MONTHLY("Monthly")
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

private fun formatDayMonth(calendar: Calendar): String {
    val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).orEmpty()
    return "${calendar.get(Calendar.DAY_OF_MONTH)} $month"
}

private fun formatGroupedDateTitle(day: String): String {
    val calendar = parseDayToCalendar(day) ?: return day
    val prefix = when {
        isSameDay(calendar, Calendar.getInstance()) -> "Today"
        isSameDay(
            calendar,
            Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        ) -> "Yesterday"
        else -> formatWeekday(calendar)
    }
    return "$prefix, ${formatDayMonth(calendar)}"
}

private fun parseDayToCalendar(day: String): Calendar? {
    val parts = day.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val dayOfMonth = parts[2].toIntOrNull() ?: return null
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }
}

private fun formatWeekday(calendar: Calendar): String {
    return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()).orEmpty()
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}
