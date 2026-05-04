package com.dialysis.app.ui.weight

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

@Composable
internal fun WeightChartCard(
    xAxisLabels: List<WeightAxisLabel>,
    chartData: List<WeightChartPoint>,
    yMin: Float,
    yMax: Float,
    goalWeightKg: Float,
    chartStats: WeightChartStatsUi?
) {
    Card(modifier = Modifier.fillMaxWidth().height(276.dp), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WeightCardBackground)
                .padding16()
        ) {
            WeightChartCanvas(xAxisLabels, chartData, yMin, yMax, goalWeightKg)
            Spacer(modifier = Modifier.height(8.dp))
            WeightChartFooter(chartStats = chartStats)
        }
    }
}

@Composable
private fun WeightChartCanvas(
    xAxisLabels: List<WeightAxisLabel>,
    chartData: List<WeightChartPoint>,
    yMin: Float,
    yMax: Float,
    goalWeightKg: Float
) {
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val bounds = ChartBounds(top = 24f, bottom = size.height - 28f, left = 42f, right = size.width - 22f)
        drawGridAndAxis(bounds, yMin, yMax)
        drawXAxisLabels(bounds, xAxisLabels)
        drawGoalLine(bounds, yMin, yMax, goalWeightKg)
        drawWeightLine(bounds, chartData, yMin, yMax)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridAndAxis(
    bounds: ChartBounds,
    yMin: Float,
    yMax: Float
) {
    val lines = 5
    for (i in 0..lines) {
        val y = bounds.top + (bounds.bottom - bounds.top) * (i / lines.toFloat())
        drawLine(
            color = WeightChartGrid,
            start = Offset(bounds.left, y),
            end = Offset(bounds.right, y),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
        )
        drawContext.canvas.nativeCanvas.drawText(formatWeightValue(yMax - (yMax - yMin) * (i / lines.toFloat())), 0f, y + 8f, axisPaint())
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawXAxisLabels(
    bounds: ChartBounds,
    labels: List<WeightAxisLabel>
) {
    labels.forEach { label ->
        val x = bounds.left + (bounds.right - bounds.left) * label.xRatio
        drawContext.canvas.nativeCanvas.drawText(label.label, x, bounds.top - 8f, axisPaint(Paint.Align.CENTER))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGoalLine(
    bounds: ChartBounds,
    yMin: Float,
    yMax: Float,
    goalWeightKg: Float
) {
    if (goalWeightKg <= 0f || yMax <= yMin) return
    val goalRatio = ((goalWeightKg - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
    val goalY = bounds.bottom - (bounds.bottom - bounds.top) * goalRatio
    drawLine(
        color = Color.Gray,
        start = Offset(bounds.left, goalY),
        end = Offset(bounds.right, goalY),
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWeightLine(
    bounds: ChartBounds,
    chartData: List<WeightChartPoint>,
    yMin: Float,
    yMax: Float
) {
    if (yMax <= yMin) return
    if (chartData.size >= 2) {
        val path = Path()
        chartData.forEachIndexed { index, point ->
            val offset = point.toOffset(bounds, yMin, yMax)
            if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
        }
        drawPath(path = path, color = WeightChartLine, style = Stroke(width = 6f))
    }
    chartData.forEach { point -> drawCircle(color = WeightChartLine, radius = 6f, center = point.toOffset(bounds, yMin, yMax)) }
}

@Composable
private fun WeightChartFooter(chartStats: WeightChartStatsUi?) {
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(12.dp).height(2.dp).background(Color.Gray))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mục tiêu", style = TextStyle(fontSize = 12.sp), color = WeightTextMuted)
        }
        if (chartStats != null) {
            Text(
                "Min: ${chartStats.min?.let(::formatWeightValue) ?: "-"}  Max: ${chartStats.max?.let(::formatWeightValue) ?: "-"}  TB: ${chartStats.avg?.let(::formatWeightValue) ?: "-"}",
                style = TextStyle(fontSize = 12.sp),
                color = WeightTextMuted,
                textAlign = TextAlign.End
            )
        }
    }
}

private fun WeightChartPoint.toOffset(bounds: ChartBounds, yMin: Float, yMax: Float): Offset {
    val x = bounds.left + (bounds.right - bounds.left) * xRatio
    val yRatio = ((value - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
    return Offset(x, bounds.bottom - (bounds.bottom - bounds.top) * yRatio)
}

private fun axisPaint(align: Paint.Align = Paint.Align.LEFT): Paint {
    return Paint().apply {
        color = "#9AA0AB".toColorInt()
        textSize = 26f
        textAlign = align
        isAntiAlias = true
    }
}

private data class ChartBounds(val top: Float, val bottom: Float, val left: Float, val right: Float)
