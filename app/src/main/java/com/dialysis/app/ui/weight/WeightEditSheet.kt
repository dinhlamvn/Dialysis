package com.dialysis.app.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WeightEditSheet(
    editingMode: WeightEditingMode,
    draftWeightKg: Float,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onWeightChange: (Float) -> Unit
) {
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
            WeightSheetActionButton(text = "Hủy", onClick = onCancel, primary = false)
            WeightSheetActionButton(text = "Lưu", onClick = onSave, primary = true)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = editingMode.title,
            style = TextStyle(fontSize = 18.sp),
            color = WeightTextDark,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        WeightDraftValue(draftWeightKg)
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
private fun WeightDraftValue(draftWeightKg: Float) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
        Text(formatWeightValue(draftWeightKg).replace('.', ','), style = TextStyle(fontSize = 64.sp), color = WeightAccentBlue)
        Text(" kg", style = TextStyle(fontSize = 20.sp), color = WeightTextMuted, modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun WeightSheetActionButton(text: String, onClick: () -> Unit, primary: Boolean) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFF4F5F7),
            contentColor = if (primary) WeightAccentBlue else WeightTextDark
        )
    ) {
        Text(text, style = TextStyle(fontSize = 16.sp), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

private val WeightEditingMode.title: String
    get() = when (this) {
        WeightEditingMode.GOAL -> "Cập nhật mục tiêu"
        WeightEditingMode.INITIAL -> "Cập nhật cân nặng ban đầu"
        WeightEditingMode.CURRENT -> "Thêm cân nặng hiện tại"
    }
