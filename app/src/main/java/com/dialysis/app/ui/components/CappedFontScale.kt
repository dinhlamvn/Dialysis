package com.dialysis.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Limits the system font scale for [content]. Use only for text living inside
 * fixed-size shapes (e.g. circles, tab bars) that cannot grow with the text.
 */
@Composable
fun CappedFontScale(maxFontScale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (density.fontScale <= maxFontScale) {
        content()
        return
    }
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, maxFontScale),
        content = content
    )
}
