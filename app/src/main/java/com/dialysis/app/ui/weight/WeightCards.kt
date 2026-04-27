package com.dialysis.app.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R

@Composable
internal fun WeightGoalHeader(
    weightGoalKg: Float,
    onEditClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mục tiêu", style = TextStyle(fontSize = 16.sp), color = WeightTextMuted)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(formatWeightValue(weightGoalKg), style = TextStyle(fontSize = 48.sp), color = WeightAccentBlue)
            Text(
                " kg",
                style = TextStyle(fontSize = 20.sp),
                color = WeightTextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "✎",
                style = TextStyle(fontSize = 24.sp),
                color = WeightTextMuted,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .clickable(onClick = onEditClick)
            )
        }
    }
}

@Composable
internal fun WeightInfoCards(
    initialWeightKg: Float,
    currentWeightKg: Float,
    progressKg: Float,
    onEditInitialWeightClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallWeightInfoCard(
                title = stringResource(R.string.weight_initial),
                value = "${formatWeightValue(initialWeightKg)} kg",
                valueColor = WeightTextDark,
                editable = true,
                onClick = onEditInitialWeightClick
            )
            SmallWeightInfoCard(
                title = stringResource(R.string.weight_current),
                value = "${formatWeightValue(currentWeightKg)} kg",
                valueColor = WeightAccentBlue
            )
        }

        WeightProgressCard(progressKg = progressKg, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SmallWeightInfoCard(
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WeightCardBackground)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = TextStyle(fontSize = 14.sp), color = WeightTextMuted)
                Spacer(modifier = Modifier.weight(1f))
                if (editable) {
                    Text("✎", style = TextStyle(fontSize = 22.sp), color = WeightTextMuted)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = TextStyle(fontSize = 20.sp), color = valueColor)
        }
    }
}

@Composable
private fun WeightProgressCard(progressKg: Float, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(212.dp), shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WeightCardBackground)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tiến độ cân nặng", style = TextStyle(fontSize = 14.sp), color = WeightTextMuted)
                Spacer(modifier = Modifier.weight(1f))
                Text(">", style = TextStyle(fontSize = 12.sp), color = WeightTextMuted)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                buildProgressText(progressKg),
                style = TextStyle(fontSize = 20.sp),
                color = if (progressKg >= 0f) WeightPositiveGreen else WeightNegativeRed
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text("⚖", style = TextStyle(fontSize = 40.sp), color = WeightScaleOrange)
            }
        }
    }
}
