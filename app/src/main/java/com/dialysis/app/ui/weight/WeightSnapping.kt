package com.dialysis.app.ui.weight

import kotlin.math.roundToInt

internal const val MIN_WEIGHT_KG = 25f
internal const val MAX_WEIGHT_KG = 200f
internal const val WEIGHT_STEP_KG = 0.5f

/** Rounds [value] to the nearest [WEIGHT_STEP_KG] (halves round up) and clamps it to the weight range. */
internal fun snapWeightKg(value: Float): Float {
    if (value.isNaN()) return MIN_WEIGHT_KG
    // Clamp first: the range ends are step-aligned, so the rounded result stays in range.
    val clamped = value.coerceIn(MIN_WEIGHT_KG, MAX_WEIGHT_KG)
    return (clamped / WEIGHT_STEP_KG).roundToInt() * WEIGHT_STEP_KG
}

/** Number of ruler ticks: one per [WEIGHT_STEP_KG] from [MIN_WEIGHT_KG] to [MAX_WEIGHT_KG], both ends included. */
internal val WEIGHT_TICK_COUNT: Int = ((MAX_WEIGHT_KG - MIN_WEIGHT_KG) / WEIGHT_STEP_KG).roundToInt() + 1

/** Ruler tick index of [weightKg] after snapping; index 0 is [MIN_WEIGHT_KG]. NaN maps to index 0. */
internal fun weightKgToRulerIndex(weightKg: Float): Int =
    ((snapWeightKg(weightKg) - MIN_WEIGHT_KG) / WEIGHT_STEP_KG).roundToInt().coerceIn(0, WEIGHT_TICK_COUNT - 1)

/** Weight of ruler tick [index], clamped to the ruler; computed from the index so no float error accumulates. */
internal fun rulerIndexToWeightKg(index: Int): Float =
    MIN_WEIGHT_KG + index.coerceIn(0, WEIGHT_TICK_COUNT - 1) * WEIGHT_STEP_KG

/**
 * Ruler tick under the center indicator, from the ruler's scroll position. Ticks are [tickPx] apart and the ruler is
 * padded so that tick i is centered at scroll position (i, 0); a tick wins once it is at least half-way in.
 */
internal fun rulerCenterIndex(firstVisibleIndex: Int, firstVisibleOffsetPx: Int, tickPx: Int): Int {
    val next = if (tickPx > 0 && firstVisibleOffsetPx * 2 >= tickPx) 1 else 0
    return (firstVisibleIndex + next).coerceIn(0, WEIGHT_TICK_COUNT - 1)
}
