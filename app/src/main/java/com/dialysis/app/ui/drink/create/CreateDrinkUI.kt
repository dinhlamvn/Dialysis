package com.dialysis.app.ui.drink.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dialysis.app.R
import com.dialysis.app.ui.components.PrimaryButton

private val PageBackground = Color(0xFFFFFFFF)
private val AccentBlue = Color(0xFF1877F2)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val CardBackground = Color(0xFFF1F1F6)
private val CardBorder = Color(0xFFCED2DA)

@Composable
fun CreateDrinkScreen() {
    var selectedId by remember { mutableStateOf("200_ml") }

    val options = listOf(
        DrinkOption("30_ml", "30 ml", stringResource(R.string.drink_unit_espresso)),
        DrinkOption("50_ml", "50 ml", stringResource(R.string.drink_unit_glass)),
        DrinkOption("100_ml", "100 ml", null),
        DrinkOption("150_ml", "150 ml", null),
        DrinkOption("200_ml", "200 ml", stringResource(R.string.drink_unit_glass)),
        DrinkOption("250_ml", "250 ml", stringResource(R.string.drink_unit_cup)),
        DrinkOption("300_ml", "300 ml", null),
        DrinkOption("330_ml", "330 ml", null),
        DrinkOption("400_ml", "400 ml", null),
        DrinkOption("500_ml", "500 ml", null),
        DrinkOption("600_ml", "600 ml", null),
        DrinkOption("800_ml", "800 ml", null),
        DrinkOption("1000_ml", "1,000 ml", stringResource(R.string.drink_unit_litre)),
        DrinkOption("custom", stringResource(R.string.create_drink_custom), null)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopBar()

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.create_drink_title),
            color = TextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.create_drink_amount),
            color = AccentBlue,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.create_drink_subtitle),
            color = TextMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(options) { option ->
                VolumeCard(
                    option = option,
                    selected = selectedId == option.id,
                    onClick = { selectedId = option.id }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.create_drink_time),
                color = TextMuted,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            BoxPencil()
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = stringResource(R.string.create_drink_add),
            onClick = {},
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            color = TextDark,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun VolumeCard(option: DrinkOption, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) AccentBlue else Color.Transparent
    val textColor = if (selected) AccentBlue else TextDark

    Card(
        modifier = Modifier
            .height(78.dp)
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(CardBackground, RoundedCornerShape(16.dp))
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = option.value,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            if (!option.label.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.label,
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoxPencil() {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color(0xFFE6E6EB)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "✎", fontSize = 10.sp, color = TextMuted)
    }
}

private data class DrinkOption(
    val id: String,
    val value: String,
    val label: String?
)
