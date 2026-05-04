package com.dialysis.app.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dialysis.app.router.Router
import com.dialysis.app.ui.components.TextStyles

@Composable
internal fun WeightBottomNav() {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), shape = RoundedCornerShape(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeightWhite)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeightBottomItem(label = "Nước của tôi", active = false, onClick = { context.startActivity(Router.home(context)) })
            WeightBottomItem(label = "Cân nặng", active = true, onClick = {})
            WeightFloatingAddButton()
            WeightBottomItem(label = "Thống kê", active = false, onClick = {})
            WeightBottomItem(label = "Cài đặt", active = false, onClick = {})
        }
    }
}

@Composable
private fun WeightBottomItem(label: String, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(modifier = Modifier.size(24.dp).background(if (active) WeightAccentBlue else androidx.compose.ui.graphics.Color(0xFFBCC2CC), CircleShape))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = if (active) WeightAccentBlue else WeightTextMuted, style = TextStyles.caption)
    }
}

@Composable
private fun WeightFloatingAddButton() {
    Button(
        onClick = {},
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = WeightAccentBlue),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = "+", color = WeightWhite, style = TextStyles.titleMedium)
    }
}
