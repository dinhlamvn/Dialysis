package com.dialysis.app.ui.drink.create

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dialysis.app.R
import com.dialysis.app.ui.components.PrimaryButton
import com.dialysis.app.ui.components.TextStyles
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val PageBackground = Color(0xFFFFFFFF)
private val AccentBlue = Color(0xFF1877F2)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val CardBackground = Color(0xFFF1F1F6)
private val CardBorder = Color(0xFFCED2DA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDrinkScreen(
    drinkName: String = "",
    viewModel: CreateDrinkViewModel = viewModel(),
    onAddDrink: (drinkName: String, amount: String, time: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val stateDrinkName by viewModel.drinkNameState.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedIdState.collectAsStateWithLifecycle()
    val showCustomInputSheet by viewModel.showCustomInputSheetState.collectAsStateWithLifecycle()
    val customInput by viewModel.customInputState.collectAsStateWithLifecycle()
    val customMl by viewModel.customMlState.collectAsStateWithLifecycle()
    val selectedTimeText by viewModel.selectedTimeTextState.collectAsStateWithLifecycle()

    LaunchedEffect(drinkName) {
        viewModel.updateDrinkName(drinkName)
        viewModel.resetForm(currentTimeText())
    }

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
    val selectedAmountText = when {
        selectedId == "custom" && customMl != null -> "${customMl} ml"
        else -> options.firstOrNull { it.id == selectedId }?.value
            ?: stringResource(R.string.create_drink_amount)
    }
    val fallbackDrinkTitle = stringResource(R.string.create_drink_title)
    val displayDrinkName = if (stateDrinkName.isNotBlank()) stateDrinkName else fallbackDrinkTitle
    val displayTimeText = selectedTimeText.ifBlank { currentTimeText() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = displayDrinkName,
            color = TextDark,
            style = TextStyles.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = selectedAmountText,
            color = AccentBlue,
            style = TextStyles.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.create_drink_subtitle),
            color = TextMuted,
            style = TextStyles.body,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(options) { option ->
                val displayOption = if (option.id == "custom" && customMl != null) {
                    option.copy(value = "${customMl} ml")
                } else {
                    option
                }
                VolumeCard(
                    option = displayOption,
                    selected = selectedId == option.id,
                    onClick = {
                        if (option.id == "custom") {
                            viewModel.updateSelectedId("custom")
                            viewModel.updateCustomInput(customMl?.toString().orEmpty())
                            viewModel.updateShowCustomInputSheet(true)
                        } else {
                            viewModel.updateSelectedId(option.id)
                        }
                    }
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
                text = displayTimeText,
                color = TextMuted,
                style = TextStyles.titleMedium
            )
            Spacer(modifier = Modifier.width(10.dp))
            BoxPencil(
                onClick = {
                    showTimePickerDialog(context) { hour, minute ->
                        viewModel.updateSelectedTimeText(formatTime(hour, minute))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.create_drink_add),
            onClick = {
                onAddDrink(displayDrinkName, selectedAmountText, displayTimeText)
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showCustomInputSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.updateShowCustomInputSheet(false) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_drink_custom_input_title),
                    color = TextDark,
                    style = TextStyles.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customInput,
                    onValueChange = { value -> viewModel.updateCustomInput(value.take(8)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Text(
                            text = stringResource(R.string.create_drink_custom_input_label),
                            style = TextStyles.body
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.updateShowCustomInputSheet(false) }) {
                        Text(
                            text = stringResource(R.string.common_cancel),
                            style = TextStyles.body
                        )
                    }
                    TextButton(
                        onClick = {
                            val ml = customInput.filter { it.isDigit() }.toIntOrNull()
                            if (ml != null && ml > 0) {
                                viewModel.applyCustomMl(ml)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.otp_verify_confirm),
                            style = TextStyles.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
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
                style = TextStyles.titleMedium,
                textAlign = TextAlign.Center
            )
            if (!option.label.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.label,
                    color = TextMuted,
                    style = TextStyles.caption,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoxPencil(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color(0xFFE6E6EB))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "✎", style = TextStyles.caption, color = TextMuted)
    }
}

private fun showTimePickerDialog(
    context: Context,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val current = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(hour, minute) },
        current.get(Calendar.HOUR_OF_DAY),
        current.get(Calendar.MINUTE),
        false
    ).show()
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale("vi", "VN"))
        .format(calendar.time)
        .replace("AM", "SA")
        .replace("PM", "CH")
}

private fun currentTimeText(): String {
    val current = Calendar.getInstance()
    return formatTime(
        hour = current.get(Calendar.HOUR_OF_DAY),
        minute = current.get(Calendar.MINUTE)
    )
}

private data class DrinkOption(
    val id: String,
    val value: String,
    val label: String?
)
