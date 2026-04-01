package com.dialysis.app.ui.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dialysis.app.R
import com.dialysis.app.ui.components.PrimaryButton
import com.dialysis.app.ui.components.TextStyles
import com.dialysis.app.ui.drink.create.CreateDrinkScreen
import com.dialysis.app.ui.drink.list.DrinkListScreen
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val PageBackground = Color(0xFFFFFFFF)
private val AccentBlue = Color(0xFF1877F2)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF8E8E93)
private val CardSurface = Color(0xFFF8F8FB)
private val IconTint = Color(0xFFF2B04A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(viewModel: DailyReportViewModel) {
    val selectedDateLabel by viewModel.selectedDateLabelState.collectAsStateWithLifecycle()
    val drinks by viewModel.drinksState.collectAsStateWithLifecycle()
    val progress by viewModel.progressState.collectAsStateWithLifecycle()
    val progressText by viewModel.progressTextState.collectAsStateWithLifecycle()
    val showDrinkListSheet by viewModel.showDrinkListSheetState.collectAsStateWithLifecycle()
    val selectedDrinkName by viewModel.selectedDrinkNameState.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialogState.collectAsStateWithLifecycle()
    val deletingDrinkTitle by viewModel.deletingDrinkTitleState.collectAsStateWithLifecycle()
    val drinkListSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val createDrinkSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopBar(selectedDateLabel = selectedDateLabel)

        Spacer(modifier = Modifier.height(20.dp))

        ProgressBar(
            progress = progress,
            progressText = progressText
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items = drinks, key = { it.id }) { drink ->
                DrinkItem(
                    title = drink.title,
                    subtitle = drink.subtitle,
                    time = drink.time,
                    onDeleteClick = { viewModel.requestDelete(drink) }
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.daily_add_drink),
            onClick = viewModel::openDrinkListSheet,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showDrinkListSheet) {
        ModalBottomSheet(
            sheetState = drinkListSheetState,
            onDismissRequest = viewModel::closeDrinkListSheet
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                DrinkListScreen(
                    onDrinkClick = viewModel::onDrinkSelected
                )
            }
        }
    }

    selectedDrinkName?.let { drinkName ->
        ModalBottomSheet(
            sheetState = createDrinkSheetState,
            onDismissRequest = viewModel::dismissCreateDrinkSheet
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                CreateDrinkScreen(
                    drinkName = drinkName,
                    onBackClick = viewModel::backToDrinkListFromCreate,
                    onAddDrink = { name, amount, time ->
                        viewModel.addDrink(name, amount, time)
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = {
                Text(text = "Xác nhận xóa", style = TextStyles.titleMedium, color = TextDark)
            },
            text = {
                Text(
                    text = "Bạn có muốn xóa \"$deletingDrinkTitle\" không?",
                    style = TextStyles.body,
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteDrink) {
                    Text(text = "OK", style = TextStyles.bodyMedium, color = AccentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text(text = "Hủy", style = TextStyles.body)
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    selectedDateLabel: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedDateLabel.ifBlank { stringResource(R.string.daily_date) },
            color = TextDark,
            style = TextStyles.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    progressText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFFE6E6EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(AccentBlue)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = progressText,
            color = TextDark,
            style = TextStyles.titleMedium
        )
    }
}

@Composable
private fun DrinkItem(
    title: String,
    subtitle: String,
    time: String,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(CardSurface)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE9F1FF)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(IconTint)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextDark,
                    style = TextStyles.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    style = TextStyles.body
                )
            }

            Text(
                text = time,
                color = TextMuted,
                style = TextStyles.body
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF4E3))
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🗑",
                    style = TextStyles.body
                )
            }
        }
    }
}
