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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dialysis.app.R
import com.dialysis.app.data.local.model.DailyTotal
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.Loading
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.daily.DailyReportScreen
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.drink.DrinkCatalog
import com.dialysis.app.ui.drink.create.CreateDrinkScreen
import com.dialysis.app.ui.drink.list.DrinkListScreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val BlueTop = Color(0xFF2D6FDD)
private val BlueBottom = Color(0xFF1A50C9)
private val CardLight = Color(0xFFF3F3F7)
private val CardGray = Color(0xFFBFC3CB)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val AccentBlue = Color(0xFF1877F2)
private val WaterYellow = Color(0xFFFFC107)
private val WaterOrange = Color(0xFFFF9800)
private val WaterRed = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    dailyReportViewModel: DailyReportViewModel,
    showBottomNav: Boolean = true,
    onStatisticsMoreClick: () -> Unit = {},
    onWeightProgressClick: () -> Unit = {}
) {
    val drinks by viewModel.drinksState.collectAsStateWithLifecycle()
    val todayTotalMl by viewModel.todayTotalMlState.collectAsStateWithLifecycle()
    val dailyTotals by viewModel.dailyTotalsState.collectAsStateWithLifecycle()
    val dailyWaterGoalMl by viewModel.dailyWaterGoalMlState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedInState.collectAsStateWithLifecycle()
    val showSymptomSheet by viewModel.showSymptomSheetState.collectAsStateWithLifecycle()
    val symptoms by viewModel.symptomsState.collectAsStateWithLifecycle()
    val selectedSymptom by viewModel.selectedSymptomState.collectAsStateWithLifecycle()
    val symptomNotes by viewModel.symptomNotesState.collectAsStateWithLifecycle()
    val isSymptomsLoading by viewModel.isSymptomsLoadingState.collectAsStateWithLifecycle()
    val isSubmittingSymptom by viewModel.isSubmittingSymptomState.collectAsStateWithLifecycle()
    val rolling7DayStats = buildLast7DayStats(dailyTotals)
    val showDrinkListSheet by viewModel.showDrinkListSheetState.collectAsStateWithLifecycle()
    val showDailyReportSheet by viewModel.showDailyReportSheetState.collectAsStateWithLifecycle()
    val selectedDrinkName by viewModel.selectedDrinkNameState.collectAsStateWithLifecycle()
    val drinkListSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dailyReportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val createDrinkSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val symptomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HeaderCard(
                    todayTotalMl = todayTotalMl,
                    goalMl = dailyWaterGoalMl
                )
            }

            item {
                DrinksSection(
                    drinks = drinks,
                    onAddDrinkClick = viewModel::openDrinkListSheet,
                    onEditClick = {
                        dailyReportViewModel.showTodayReport()
                        viewModel.openDailyReportSheet()
                    }
                )
            }

            item {
                StatisticsListSection(
                    weekStats = rolling7DayStats,
                    goalMl = dailyWaterGoalMl,
                    isLoggedIn = isLoggedIn,
                    onSymptomClick = viewModel::openSymptomSheet,
                    onMoreClick = onStatisticsMoreClick,
                    onWeightProgressClick = onWeightProgressClick,
                    onDayClick = { dateMillis ->
                        dailyReportViewModel.showDateReport(dateMillis)
                        viewModel.openDailyReportSheet()
                    }
                )
            }
        }

        if (showBottomNav) {
            BottomNav()
        }
    }

    if (showDrinkListSheet) {
        ModalBottomSheet(
            sheetState = drinkListSheetState,
            onDismissRequest = viewModel::closeDrinkListSheet
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                DrinkListScreen(
                    onDrinkClick = viewModel::onDrinkSelected
                )
            }
        }
    }

    if (showDailyReportSheet) {
        ModalBottomSheet(
            sheetState = dailyReportSheetState,
            onDismissRequest = {
                viewModel.closeDailyReportSheet()
                dailyReportViewModel.showTodayReport()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                DailyReportScreen(
                    viewModel = dailyReportViewModel,
                    onBackClick = {
                        viewModel.closeDailyReportSheet()
                        dailyReportViewModel.showTodayReport()
                    }
                )
            }
        }
    }

    selectedDrinkName?.let { drinkName ->
        ModalBottomSheet(
            sheetState = createDrinkSheetState,
            onDismissRequest = viewModel::dismissCreateDrinkSheet
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                CreateDrinkScreen(
                    drinkName = drinkName,
                    onBackClick = viewModel::backToDrinkListFromCreate,
                    onAddDrink = { name, amount, time ->
                        viewModel.addDrink(name = name, amount = amount, _time = time)
                    }
                )
            }
        }
    }

    if (showSymptomSheet) {
        ModalBottomSheet(
            sheetState = symptomSheetState,
            onDismissRequest = viewModel::closeSymptomSheet
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                SymptomReportSheet(
                    symptoms = symptoms,
                    selectedSymptom = selectedSymptom,
                    notes = symptomNotes,
                    isSymptomsLoading = isSymptomsLoading,
                    isSubmitting = isSubmittingSymptom,
                    onCancel = viewModel::closeSymptomSheet,
                    onSelectSymptom = viewModel::selectSymptom,
                    onNotesChange = viewModel::updateSymptomNotes,
                    onSubmit = viewModel::submitSymptomLog
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(todayTotalMl: Int, goalMl: Int) {
    val progress = (todayTotalMl / goalMl.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()
    val waterLevelColor = when {
        progressPercent > 80 -> WaterRed
        progressPercent > 70 -> WaterOrange
        progressPercent > 50 -> WaterYellow
        else -> Color.White
    }

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
                    WaterCircle(
                        progress = progress,
                        waterColor = waterLevelColor
                    )
                    Column {
                        Text(
                            text = "$progressPercent%",
                            color = Color.White,
                            style = TextStyles.titleMedium
                        )
                        Text(
                            text = "${todayTotalMl}ml của ${goalMl / 1000f}l",
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
private fun WaterCircle(
    progress: Float,
    waterColor: Color
) {
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
                    .height((88.dp * progress).coerceIn(0.dp, 88.dp))
                    .background(waterColor)
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
private fun DrinksSection(
    drinks: List<HomeDrinkItemState>,
    onAddDrinkClick: () -> Unit,
    onEditClick: () -> Unit
) {
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
                modifier = Modifier.clickable(onClick = onEditClick)
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
                    subtitle = stringResource(R.string.home_add),
                    onClick = onAddDrinkClick
                )
            }
            drinks.forEach { drink ->
                item {
                    DrinkCard(
                        amount = drink.amount,
                        name = drink.name,
                        time = drink.time
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallDrinkCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .size(width = 110.dp, height = 130.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
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
    val visual = DrinkCatalog.resolve(name)
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
                    .background(Color(0xFFF8F8FB)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(visual.tileGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = visual.icon,
                        color = visual.iconTextColor,
                        style = TextStyles.caption
                    )
                }
            }
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
private fun WeeklySection(
    onMoreClick: () -> Unit
) {
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
                style = TextStyles.bodyMedium,
                modifier = Modifier.clickable { onMoreClick() }
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
private fun StatisticsListSection(
    weekStats: List<RollingDayStat>,
    goalMl: Int,
    isLoggedIn: Boolean,
    onSymptomClick: () -> Unit,
    onMoreClick: () -> Unit,
    onWeightProgressClick: () -> Unit,
    onDayClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                style = TextStyles.bodyMedium,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }

        StatsChartCard(
            weekStats = weekStats,
            goalMl = goalMl,
            onDayClick = onDayClick
        )
        if (isLoggedIn) {
            SymptomCard(onClick = onSymptomClick)
        }
        BannerCard(
            background = Color(0xFFFF6B39),
            title = "Weight progress  >",
            description = "Track your weight dynamics and\nenhance your well-being with My\nWater",
            titleColor = Color.White,
            descriptionColor = Color.White.copy(alpha = 0.95f),
            onClick = onWeightProgressClick
        )
        TipOfDayCard()
    }
}

@Composable
private fun SymptomCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFF4E5E))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚠  Ghi nhận triệu chứng",
                color = Color.White,
                style = TextStyles.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Báo cáo triệu chứng nghiêm trọng",
                color = Color.White.copy(alpha = 0.92f),
                style = TextStyles.body
            )
        }
    }
}

@Composable
private fun SymptomReportSheet(
    symptoms: List<String>,
    selectedSymptom: String?,
    notes: String,
    isSymptomsLoading: Boolean,
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onSelectSymptom: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    onClick = onCancel
                ) {
                    Text(
                        text = "Huỷ",
                        style = TextStyles.bodyMedium,
                        color = TextDark,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                Text(
                    text = "Ghi nhận triệu chứng",
                    style = TextStyles.titleMedium,
                    color = TextDark
                )
                Spacer(modifier = Modifier.width(56.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nếu bạn đang gặp tình trạng khẩn cấp, vui lòng liên hệ ngay với bác sĩ hoặc gọi cấp cứu",
                color = TextMuted,
                style = TextStyles.body
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Triệu chứng", color = TextDark, style = TextStyles.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (isSymptomsLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Loading()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(symptoms.size) { index ->
                        val symptom = symptoms[index]
                        val isSelected = symptom == selectedSymptom
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFFFF1F3) else CardLight)
                                .clickable { onSelectSymptom(symptom) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectSymptom(symptom) },
                                modifier = Modifier
                                    .scale(0.85f)
                                    .size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = symptom,
                                color = TextDark,
                                style = TextStyles.body
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Ghi chú thêm", color = TextDark, style = TextStyles.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = {
                    Text(
                        text = "Nhập chi tiết triệu chứng",
                        color = TextMuted,
                        style = TextStyles.body
                    )
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onSubmit,
                enabled = selectedSymptom != null && notes.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4E5E)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "Lưu báo cáo",
                    color = Color.White,
                    style = TextStyles.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (isSubmitting) {
            Loading(overlayColor = Color.Black.copy(alpha = 0.2f))
        }
    }
}

@Composable
private fun StatsChartCard(
    weekStats: List<RollingDayStat>,
    goalMl: Int,
    onDayClick: (Long) -> Unit
) {
    val safeStats = if (weekStats.size == 7) weekStats else List(7) { RollingDayStat("", 0, 0L) }
    val weekTotalMl = safeStats.sumOf { it.totalMl }
    val averagePercent = safeStats
        .sumOf { stat -> ((stat.totalMl / goalMl.toFloat()) * 100f).coerceAtLeast(0f).toDouble() }
        .toFloat() / 7f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF1D8EF8), Color(0xFF3C9BEA))))
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    safeStats.forEach { dayStat ->
                        val progress = (dayStat.totalMl / goalMl.toFloat()).coerceIn(0f, 1f)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { onDayClick(dayStat.dateMillis) },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidthPx = 7.5f
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.24f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                    )
                                    if (progress > 0f) {
                                        drawArc(
                                            color = Color.White,
                                            startAngle = -90f,
                                            sweepAngle = 360f * progress,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                Text(
                                    text = dayStat.label,
                                    color = Color.White,
                                    style = TextStyles.caption
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatMlWithGrouping(dayStat.totalMl),
                                color = Color.White.copy(alpha = 0.92f),
                                style = TextStyles.caption,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.height(70.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = stringResource(R.string.home_weekly_avg),
                                color = Color.White.copy(alpha = 0.82f),
                                style = TextStyles.title
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f%%", averagePercent),
                            color = Color.White,
                            style = TextStyles.titleMedium
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier.height(70.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Text(
                                text = stringResource(R.string.home_weekly_total),
                                color = Color.White.copy(alpha = 0.82f),
                                style = TextStyles.title,
                                textAlign = TextAlign.End
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatMlWithGrouping(weekTotalMl),
                            color = Color.White,
                            style = TextStyles.titleMedium,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun buildLast7DayStats(dailyTotals: List<DailyTotal>): List<RollingDayStat> {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val byDay = dailyTotals.associate { it.day to it.totalMl }
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return (6 downTo 0).map { daysAgo ->
        val dayMillis = calendar.timeInMillis - daysAgo * MILLIS_IN_DAY
        val dayKey = formatter.format(Date(dayMillis))
        val dayCalendar = Calendar.getInstance().apply { timeInMillis = dayMillis }
        RollingDayStat(
            label = formatVietnameseDayLabel(dayCalendar.get(Calendar.DAY_OF_WEEK)),
            totalMl = byDay[dayKey] ?: 0,
            dateMillis = dayMillis
        )
    }
}

private fun formatVietnameseDayLabel(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.MONDAY -> "Th 2"
        Calendar.TUESDAY -> "Th 3"
        Calendar.WEDNESDAY -> "Th 4"
        Calendar.THURSDAY -> "Th 5"
        Calendar.FRIDAY -> "Th 6"
        Calendar.SATURDAY -> "Th 7"
        Calendar.SUNDAY -> "CN"
        else -> ""
    }
}

private fun formatMlWithGrouping(valueMl: Int): String {
    val grouped = String.format(Locale.US, "%,d", valueMl).replace(',', '.')
    return "$grouped ml"
}

private const val MILLIS_IN_DAY = 24L * 60L * 60L * 1000L
private data class RollingDayStat(
    val label: String,
    val totalMl: Int,
    val dateMillis: Long
)

@Composable
private fun BannerCard(
    background: Color,
    title: String,
    description: String,
    titleColor: Color,
    descriptionColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(20.dp)
        ) {
            Text(text = title, color = titleColor, style = TextStyles.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = description, color = descriptionColor, style = TextStyles.body)
        }
    }
}

@Composable
private fun TipOfDayCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6B900))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.home_tip_title), color = Color.White, style = TextStyles.titleMedium)
                Text(text = stringResource(R.string.home_tip_more), color = Color.White, style = TextStyles.bodyMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_tip_desc),
                color = Color.White,
                style = TextStyles.body
            )
            Spacer(modifier = Modifier.weight(1f))
            Card(shape = RoundedCornerShape(30.dp)) {
                Text(
                    text = stringResource(R.string.home_tip_share),
                    color = Color(0xFFF6B900),
                    style = TextStyles.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNav() {
    val context = LocalContext.current
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
            BottomItem(label = stringResource(R.string.home_nav_water), active = true, onClick = {})
            BottomItem(
                label = stringResource(R.string.home_nav_weight),
                active = false,
                onClick = { context.startActivity(Router.weight(context)) }
            )
            FloatingAddButton()
            BottomItem(label = stringResource(R.string.home_nav_stats), active = false, onClick = {})
            BottomItem(label = stringResource(R.string.home_nav_settings), active = false, onClick = {})
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
