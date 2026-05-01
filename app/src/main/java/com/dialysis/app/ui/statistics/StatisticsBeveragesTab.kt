package com.dialysis.app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsBeveragesTab(
    stats: List<BeverageStatUi>,
    totalMl: Int,
    selectedPeriod: BeverageFilterPeriod,
    onPeriodChange: (BeverageFilterPeriod) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BeveragePeriodSelector(selectedPeriod = selectedPeriod, onPeriodChange = onPeriodChange)
        DonutChart(stats = stats.filter { it.volumeMl > 0 }, totalMl = totalMl)
        stats.forEach { stat -> BeverageRow(stat) }
    }
}

@Composable
private fun BeveragePeriodSelector(selectedPeriod: BeverageFilterPeriod, onPeriodChange: (BeverageFilterPeriod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = "${selectedPeriod.title}⌄",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { expanded = true }.padding(vertical = 8.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BeverageFilterPeriod.entries.forEach { period ->
                DropdownMenuItem(text = { Text(period.title) }, onClick = {
                    expanded = false
                    onPeriodChange(period)
                })
            }
        }
    }
}

@Composable
private fun DonutChart(stats: List<BeverageStatUi>, totalMl: Int) {
    Box(
        modifier = Modifier.fillMaxWidth().height(232.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val stroke = Stroke(width = 34.dp.toPx(), cap = StrokeCap.Butt)
            if (totalMl <= 0) {
                drawCircle(Color.Gray.copy(alpha = 0.25f), style = Stroke(width = 2.dp.toPx()))
            } else {
                var start = -90f
                stats.forEach { stat ->
                    val sweep = (stat.volumeMl / totalMl.toFloat()) * 360f
                    drawArc(stat.visual.color, start, sweep, false, style = stroke)
                    start += sweep
                }
            }
        }
        Text(formatMl(totalMl), color = Color(0xFF1877F2), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BeverageRow(stat: BeverageStatUi) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF2F3F6), RoundedCornerShape(16.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) { drawCircle(stat.visual.color, center = Offset(size.width / 2, size.height / 2)) }
        Text(stat.visual.icon, fontSize = 24.sp)
        Text(stat.visual.title, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text(formatMl(stat.volumeMl), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text("${stat.percentage}%", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(44.dp))
    }
}
