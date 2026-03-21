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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dialysis.app.R
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.daily.DailyReportScreen
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.drink.create.CreateDrinkScreen
import com.dialysis.app.ui.drink.list.DrinkListScreen

private val BlueTop = Color(0xFF2D6FDD)
private val BlueBottom = Color(0xFF1A50C9)
private val CardLight = Color(0xFFF3F3F7)
private val CardGray = Color(0xFFBFC3CB)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val AccentBlue = Color(0xFF1877F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    dailyReportViewModel: DailyReportViewModel,
    showBottomNav: Boolean = true
) {
    val drinks by viewModel.drinksState.collectAsStateWithLifecycle()
    val showDrinkListSheet by viewModel.showDrinkListSheetState.collectAsStateWithLifecycle()
    val showDailyReportSheet by viewModel.showDailyReportSheetState.collectAsStateWithLifecycle()
    val selectedDrinkName by viewModel.selectedDrinkNameState.collectAsStateWithLifecycle()
    val drinkListSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dailyReportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val createDrinkSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                HeaderCard()
            }

            item {
                DrinksSection(
                    drinks = drinks,
                    onAddDrinkClick = viewModel::openDrinkListSheet,
                    onEditClick = viewModel::openDailyReportSheet
                )
            }

            item {
                StatisticsListSection(onMoreClick = viewModel::openDrinkListSheet)
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
            onDismissRequest = viewModel::closeDailyReportSheet
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                DailyReportScreen(viewModel = dailyReportViewModel)
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
                    onAddDrink = { name, amount, time ->
                        viewModel.addDrink(name = name, amount = amount, _time = time)
                    }
                )
            }
        }
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
private fun StatisticsListSection(onMoreClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Statistics for the week", color = TextDark, style = TextStyles.titleMedium)
            Text(
                text = "More",
                color = AccentBlue,
                style = TextStyles.bodyMedium,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }

        StatsChartCard()
        BannerCard(
            background = Color(0xFFFF6B39),
            title = "Weight progress  >",
            description = "Track your weight dynamics and\nenhance your well-being with My\nWater",
            titleColor = Color.White,
            descriptionColor = Color.White.copy(alpha = 0.95f)
        )
        BannerCard(
            background = Color(0xFFE8EBEF),
            title = "Full version\nTry 3 days for free",
            description = "Full version will help you reach your\ndaily goal faster!",
            titleColor = AccentBlue,
            descriptionColor = TextDark.copy(alpha = 0.85f)
        )
        TipOfDayCard()
    }
}

@Composable
private fun StatsChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF8FD300), Color(0xFF00C53A))))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = "Goal: 1.9l", color = Color.White, style = TextStyles.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                WeekChart()
            }
        }
    }
}

@Composable
private fun WeekChart() {
    val yLabels = listOf("2.4 l", "1.6 l", "0.8 l", "0 l")
    val days = listOf("Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color.White.copy(alpha = 0.25f)
                val goalColor = Color.White.copy(alpha = 0.85f)
                val lines = listOf(0.08f, 0.33f, 0.62f, 0.92f)
                lines.forEachIndexed { index, ratio ->
                    val y = size.height * ratio
                    drawLine(
                        color = if (index == 1) goalColor else gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (index == 1) 4f else 2f,
                        pathEffect = if (index == 1) {
                            PathEffect.dashPathEffect(floatArrayOf(22f, 12f), 0f)
                        } else {
                            null
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yLabels.forEach { label ->
                    Text(text = label, color = Color.White, style = TextStyles.caption)
                }
            }

            Text(
                text = "Goal",
                color = Color.White,
                style = TextStyles.bodyMedium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 44.dp, top = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "0 l", color = Color.White, style = TextStyles.caption)
                    Text(text = day, color = Color.White, style = TextStyles.body)
                }
            }
        }
    }
}

@Composable
private fun BannerCard(
    background: Color,
    title: String,
    description: String,
    titleColor: Color,
    descriptionColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
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
                Text(text = "Tip of the day", color = Color.White, style = TextStyles.titleMedium)
                Text(text = "more tips  >", color = Color.White, style = TextStyles.bodyMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Having your first glass of water as\nsoon as you wake up can normalize\nyour bodily functions and top up your\nwater balance.",
                color = Color.White,
                style = TextStyles.body
            )
            Spacer(modifier = Modifier.weight(1f))
            Card(shape = RoundedCornerShape(30.dp)) {
                Text(
                    text = "Share advice",
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
