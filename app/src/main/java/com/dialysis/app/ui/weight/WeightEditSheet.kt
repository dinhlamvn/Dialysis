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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter

@Composable
internal fun WeightEditSheet(
    editingMode: WeightEditingMode,
    draftWeightKg: Float,
    onCancel: () -> Unit,
    onSave: (Float) -> Unit,
    onWeightChange: (Float) -> Unit
) {
    // The ruler's own scroll state is the source of the shown value: ViewModel updates arrive asynchronously
    // (StateManager channel), so they must never move the ruler while the user scrolls.
    val rulerState = rememberWeightRulerState(key = editingMode, initialKg = draftWeightKg)
    val shownKg by rememberRulerCenterWeightKg(rulerState)
    val sentWeights = remember(editingMode) { SentWeights() }
    val latestDraftKg by rememberUpdatedState(draftWeightKg)
    val latestOnWeightChange by rememberUpdatedState(onWeightChange)

    fun sendWeight(weightKg: Float) {
        if (weightKg == (sentWeights.last ?: snapWeightKg(latestDraftKg))) return
        sentWeights.add(weightKg)
        latestOnWeightChange(weightKg)
    }

    // Send only new snapped values, never every scroll frame.
    LaunchedEffect(rulerState) {
        snapshotFlow { shownKg }.collect { sendWeight(it) }
    }
    // Settle: make sure the final tick reached the ViewModel.
    LaunchedEffect(rulerState) {
        snapshotFlow { rulerState.isScrollInProgress }.filter { !it }.collect { sendWeight(shownKg) }
    }
    // Re-sync with a draft the ruler did not produce, while idle (e.g. the draft for a new mode arrives late).
    // Echoes of sent values may arrive late or out of order, so they never move the ruler.
    LaunchedEffect(draftWeightKg, editingMode) {
        val incomingKg = snapWeightKg(draftWeightKg)
        if (!sentWeights.isEcho(incomingKg) && !rulerState.isScrollInProgress && incomingKg != shownKg) {
            rulerState.scrollToItem(weightKgToRulerIndex(incomingKg))
        }
    }

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
            WeightSheetActionButton(text = "Lưu", onClick = { onSave(shownKg) }, primary = true)
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
        WeightDraftValue(shownKg)
        Spacer(modifier = Modifier.height(16.dp))
        WeightRulerPicker(state = rulerState)
        Spacer(modifier = Modifier.height(220.dp))
    }
}

/** Weights sent to the ViewModel for one editing mode; a draft equal to one of them is an echo of the ruler. */
internal class SentWeights {
    private val sent = HashSet<Float>()
    var last: Float? = null
        private set

    fun add(weightKg: Float) {
        sent += weightKg
        last = weightKg
    }

    fun isEcho(weightKg: Float): Boolean = weightKg in sent
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
