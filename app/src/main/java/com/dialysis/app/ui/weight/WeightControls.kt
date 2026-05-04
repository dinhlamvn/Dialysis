package com.dialysis.app.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
internal fun WeightReportTabBar(
    selectedTab: WeightReportTab,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("THỐNG KÊ", style = TextStyle(fontSize = 14.sp), color = WeightTextMuted)
        Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color(0xFFF0F2F5))
                    .padding(4.dp)
            ) {
                WeightSegmentButton(
                    text = stringResource(R.string.weight_tab_month),
                    selected = selectedTab == WeightReportTab.MONTH,
                    onClick = onMonthClick,
                    modifier = Modifier.weight(1f)
                )
                WeightSegmentButton(
                    text = stringResource(R.string.weight_tab_year),
                    selected = selectedTab == WeightReportTab.YEAR,
                    onClick = onYearClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun WeightPeriodNavigator(title: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("<", style = TextStyle(fontSize = 18.sp), color = WeightTextDark, modifier = Modifier.clickable(onClick = onPrev))
        Text(title, style = TextStyle(fontSize = 18.sp), color = WeightTextDark)
        Text(">", style = TextStyle(fontSize = 18.sp), color = WeightTextDark, modifier = Modifier.clickable(onClick = onNext))
    }
}

@Composable
private fun WeightSegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .background(if (selected) WeightWhite else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = TextStyle(fontSize = 16.sp), color = if (selected) WeightTextDark else WeightTextMuted)
    }
}
