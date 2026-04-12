package com.dialysis.app.ui.weight

import android.graphics.Paint
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dialysis.app.config.AppGoals
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.Loading
import com.dialysis.app.ui.components.TextStyles
import java.util.Locale
import kotlin.math.abs

private val AccentBlue = Color(0xFF1877F2)
private val LightCard = Color(0xFFF1F3F6)
private val TextDark = Color(0xFF2A2D34)
private val TextMuted = Color(0xFF8890A1)
private val ChartGrid = Color(0xFFD0D6DF)
private val ChartLine = Color(0xFF0E66E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    viewModel: WeightViewModel,
    showBottomNav: Boolean = true
) {
    val initialWeightKg by viewModel.initialWeightKgState.collectAsStateWithLifecycle()
    val currentWeightKg by viewModel.currentWeightKgState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTabState.collectAsStateWithLifecycle()
    val periodTitle by viewModel.periodTitleState.collectAsStateWithLifecycle()
    val chartData by viewModel.chartDataState.collectAsStateWithLifecycle()
    val xAxisLabels by viewModel.xAxisLabelsState.collectAsStateWithLifecycle()
    val yMin by viewModel.yMinState.collectAsStateWithLifecycle()
    val yMax by viewModel.yMaxState.collectAsStateWithLifecycle()
    val showAddWeightSheet by viewModel.showAddWeightSheetState.collectAsStateWithLifecycle()
    val draftWeightKg by viewModel.draftWeightKgState.collectAsStateWithLifecycle()
    val editingMode by viewModel.editingModeState.collectAsStateWithLifecycle()
    val isSavingWeight by viewModel.isSavingWeightState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val progressKg = currentWeightKg - initialWeightKg
    val progressText = buildProgressText(progressKg)

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
            item {
                GoalHeader(goalWeightKg = AppGoals.WEIGHT_GOAL_KG)
            }
            item {
                WeightInfoCards(
                    initialWeightKg = initialWeightKg,
                    currentWeightKg = currentWeightKg,
                    progressText = progressText,
                    onAddInitialWeightClick = viewModel::openInitialWeightSheet,
                    onAddCurrentWeightClick = viewModel::openCurrentWeightSheet
                )
            }
            item {
                ReportTabBar(
                    selectedTab = selectedTab,
                    onMonthClick = { viewModel.selectTab(WeightReportTab.MONTH) },
                    onYearClick = { viewModel.selectTab(WeightReportTab.YEAR) }
                )
            }
            item {
                PeriodNavigator(
                    title = periodTitle,
                    onPrev = viewModel::prevPeriod,
                    onNext = viewModel::nextPeriod
                )
            }
            item {
                ChartCard(
                    xAxisLabels = xAxisLabels,
                    chartData = chartData,
                    yMin = yMin,
                    yMax = yMax
                )
            }
        }

        if (showBottomNav) {
            WeightBottomNav()
        }
    }

    if (showAddWeightSheet) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = viewModel::closeAddWeightSheet
        ) {
            AddCurrentWeightSheet(
                editingMode = editingMode,
                draftWeightKg = draftWeightKg,
                onCancel = viewModel::closeAddWeightSheet,
                onSave = viewModel::saveDraftWeight,
                onWeightChange = viewModel::updateDraftWeight
            )
        }
    }

    if (isSavingWeight) {
        Loading(overlayColor = Color.Black.copy(alpha = 0.25f))
    }
}

@Composable
private fun GoalHeader(goalWeightKg: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Goal: ", style = TextStyles.title, color = TextMuted)
        Text(text = "$goalWeightKg kg", style = TextStyles.titleMedium, color = AccentBlue)
    }
}

@Composable
private fun WeightInfoCards(
    initialWeightKg: Float,
    currentWeightKg: Float,
    progressText: String,
    onAddInitialWeightClick: () -> Unit,
    onAddCurrentWeightClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallInfoCard(
                title = "Initial weight",
                value = "${formatWeightValue(initialWeightKg)} kg",
                highlight = false,
                onClick = onAddInitialWeightClick
            )
            SmallInfoCard(
                title = "Current weight",
                value = "${formatWeightValue(currentWeightKg)} kg",
                highlight = true,
                onClick = onAddCurrentWeightClick
            )
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
                    .clickable(onClick = onAddCurrentWeightClick)
            ) {
                Text(text = "Weight progress  >", style = TextStyles.title, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = progressText, style = TextStyles.titleMedium, color = TextDark)
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
private fun SmallInfoCard(
    title: String,
    value: String,
    highlight: Boolean,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
private fun ReportTabBar(
    selectedTab: WeightReportTab,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit
) {
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
                    .background(
                        if (selectedTab == WeightReportTab.MONTH) Color.White else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(onClick = onMonthClick)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Month", style = TextStyles.titleMedium, color = TextDark)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selectedTab == WeightReportTab.YEAR) Color.White else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(onClick = onYearClick)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Year", style = TextStyles.titleMedium, color = TextDark)
            }
        }
    }
}

@Composable
private fun PeriodNavigator(
    title: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            style = TextStyles.titleMedium,
            color = TextDark,
            modifier = Modifier.clickable(onClick = onPrev)
        )
        Text(
            text = title,
            style = TextStyles.titleMedium,
            color = TextDark
        )
        Text(
            text = ">",
            style = TextStyles.titleMedium,
            color = TextDark,
            modifier = Modifier.clickable(onClick = onNext)
        )
    }
}

@Composable
private fun ChartCard(
    xAxisLabels: List<WeightAxisLabel>,
    chartData: List<WeightChartPoint>,
    yMin: Float,
    yMax: Float
) {
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
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                val top = 24f
                val bottom = size.height - 28f
                val left = 22f
                val right = size.width - 22f

                val lines = 6
                for (i in 0..lines) {
                    val y = top + (bottom - top) * (i / lines.toFloat())
                    drawLine(
                        color = ChartGrid,
                        start = Offset(left, y),
                        end = Offset(right, y),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                }

                xAxisLabels.forEach { label ->
                    val x = left + (right - left) * label.xRatio
                    drawContext.canvas.nativeCanvas.drawText(
                        label.label,
                        x,
                        top - 8f,
                        Paint().apply {
                            color = "#9AA0AB".toColorInt()
                            textSize = 32f
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }

                if (chartData.size >= 2 && yMax > yMin) {
                    val path = Path()
                    chartData.forEachIndexed { index, point ->
                        val x = left + (right - left) * point.xRatio
                        val yRatio = ((point.value - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
                        val y = bottom - (bottom - top) * yRatio
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = ChartLine,
                        style = Stroke(width = 6f)
                    )
                } else if (chartData.size == 1 && yMax > yMin) {
                    val point = chartData.first()
                    val x = left + (right - left) * point.xRatio
                    val yRatio = ((point.value - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
                    val y = bottom - (bottom - top) * yRatio
                    drawCircle(color = ChartLine, radius = 8f, center = Offset(x, y))
                }
            }
        }
    }
}

@Composable
private fun AddCurrentWeightSheet(
    editingMode: WeightEditingMode,
    draftWeightKg: Float,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onWeightChange: (Float) -> Unit
) {
    val title = when (editingMode) {
        WeightEditingMode.INITIAL -> "Cập nhật cân nặng ban đầu"
        WeightEditingMode.CURRENT -> "Thêm cân nặng hiện tại"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SheetActionButton(text = "Hủy", onClick = onCancel, primary = false)
            SheetActionButton(text = "Lưu", onClick = onSave, primary = true)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = TextStyles.titleMedium,
            color = TextDark,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = formatWeightValue(draftWeightKg).replace('.', ','),
                style = androidx.compose.ui.text.TextStyle(fontSize = 64.sp),
                color = AccentBlue
            )
            Text(
                text = " kg",
                style = TextStyles.titleMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = draftWeightKg,
            onValueChange = { onWeightChange((it * 10f).toInt() / 10f) },
            valueRange = 25f..200f
        )
        Spacer(modifier = Modifier.height(220.dp))
    }
}

@Composable
private fun SheetActionButton(
    text: String,
    onClick: () -> Unit,
    primary: Boolean
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color(0xFFF4F5F7),
            contentColor = if (primary) AccentBlue else TextDark
        )
    ) {
        Text(
            text = text,
            style = TextStyles.titleMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun WeightBottomNav() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                label = "Nước của tôi",
                active = false,
                onClick = { context.startActivity(Router.home(context)) }
            )
            BottomItem(label = "Cân nặng", active = true, onClick = {})
            FloatingAddButton()
            BottomItem(label = "Thống kê", active = false, onClick = {})
            BottomItem(label = "Cài đặt", active = false, onClick = {})
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

private fun formatWeightValue(weight: Float): String {
    return if (abs(weight - weight.toInt()) < 0.05f) {
        weight.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", weight)
    }
}

private fun buildProgressText(progressKg: Float): String {
    val rounded = ((progressKg * 10).toInt()) / 10f
    val body = formatWeightValue(abs(rounded))
    return when {
        rounded > 0f -> "+$body kg"
        rounded < 0f -> "-$body kg"
        else -> "0 kg"
    }
}
