package com.dialysis.app.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

@Composable
internal fun WeightHistorySection(
    history: List<WeightHistoryRow>,
    onDelete: (WeightHistoryRow) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NHẬT KÝ CÂN", style = TextStyle(fontSize = 14.sp), color = WeightTextMuted)
        if (history.isEmpty()) {
            Text("Chưa có bản ghi cân nặng", style = TextStyle(fontSize = 14.sp), color = WeightTextMuted)
        } else {
            history.forEach { row -> WeightHistoryItem(row = row, onDelete = onDelete) }
        }
    }
}

@Composable
private fun WeightHistoryItem(
    row: WeightHistoryRow,
    onDelete: (WeightHistoryRow) -> Unit
) {
    Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeightCardBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatWeightHistoryDate(Date(row.dateMillis)), style = TextStyle(fontSize = 15.sp), color = WeightTextDark)
                if (row.note.isNotBlank()) {
                    Text(row.note, style = TextStyle(fontSize = 12.sp), color = WeightTextMuted)
                }
            }
            Text("${formatWeightValue(row.weightKg)} kg", style = TextStyle(fontSize = 14.sp), color = WeightAccentBlue)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "🗑",
                style = TextStyle(fontSize = 20.sp),
                color = WeightNegativeRed,
                modifier = Modifier.clickable { onDelete(row) }
            )
        }
    }
}
