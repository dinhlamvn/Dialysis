package com.dialysis.app.ui.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Distance between two ruler ticks (one tick per [WEIGHT_STEP_KG]). */
private val RulerTickSpacing = 14.dp
private val RulerHeight = 72.dp
private val RulerLabelWidth = 48.dp

/**
 * Scroll state of the weight ruler, positioned on [initialKg] without animation. A new state is created when [key]
 * (the editing mode) changes.
 */
@Composable
internal fun rememberWeightRulerState(key: Any?, initialKg: Float): LazyListState =
    rememberSaveable(key, saver = LazyListState.Saver) {
        LazyListState(firstVisibleItemIndex = weightKgToRulerIndex(initialKg))
    }

/** Snapped weight under the center indicator, read from the ruler's own scroll position. */
@Composable
internal fun rememberRulerCenterWeightKg(state: LazyListState): State<Float> {
    val tickPx = with(LocalDensity.current) { RulerTickSpacing.roundToPx() }
    return remember(state, tickPx) {
        derivedStateOf {
            rulerIndexToWeightKg(rulerCenterIndex(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset, tickPx))
        }
    }
}

/**
 * Horizontal ruler from [MIN_WEIGHT_KG] to [MAX_WEIGHT_KG]: one tick per 0.5 kg, a longer tick every 1 kg, a label
 * every 5 kg and a fixed center indicator. Flings settle exactly on a tick.
 */
@Composable
internal fun WeightRulerPicker(state: LazyListState, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(RulerHeight)
            .semantics { contentDescription = "Thước chọn cân nặng" }
    ) {
        val density = LocalDensity.current
        // Whole-px side padding so that the tick at scroll position (i, 0) sits exactly under the indicator.
        val sidePadding = with(density) {
            ((constraints.maxWidth - RulerTickSpacing.roundToPx()) / 2).coerceAtLeast(0).toDp()
        }
        LazyRow(
            state = state,
            flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center),
            contentPadding = PaddingValues(horizontal = sidePadding),
            modifier = Modifier.fillMaxWidth().height(RulerHeight)
        ) {
            items(count = WEIGHT_TICK_COUNT) { index -> RulerTick(index) }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(3.dp)
                .height(40.dp)
                .background(WeightAccentBlue, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun RulerTick(index: Int) {
    val isWholeKg = index % 2 == 0
    val hasLabel = index % 10 == 0
    Column(
        modifier = Modifier.width(RulerTickSpacing).height(RulerHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(if (isWholeKg) 2.dp else 1.dp)
                .height(
                    when {
                        hasLabel -> 32.dp
                        isWholeKg -> 22.dp
                        else -> 14.dp
                    }
                )
                .background(if (isWholeKg) WeightTextMuted else WeightChartGrid)
        )
        if (hasLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            // The label is wider than its tick cell; requiredWidth lets it overflow, centered on the tick.
            Text(
                text = formatWeightValue(rulerIndexToWeightKg(index)),
                style = TextStyle(fontSize = 13.sp),
                color = WeightTextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.requiredWidth(RulerLabelWidth)
            )
        }
    }
}
