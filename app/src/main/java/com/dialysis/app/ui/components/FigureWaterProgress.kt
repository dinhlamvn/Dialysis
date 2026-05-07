package com.dialysis.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dialysis.app.R
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sin
import org.xmlpull.v1.XmlPullParser

@Composable
fun FigureWaterProgress(
    progress: Float,
    waterColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    @DrawableRes figureResId: Int = R.drawable.figure
) {
    val context = LocalContext.current
    val figureSpec = remember(figureResId) {
        context.resources.readVectorPathSpec(figureResId)
    }
    val figurePath = remember(figureSpec) {
        Path().apply {
            figureSpec.paths.forEach { pathData ->
                addPath(PathParser().parsePathString(pathData).toPath())
            }
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "figureWaterProgress"
    )
    val transition = rememberInfiniteTransition(label = "figureWaterWave")
    val primaryWavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "primaryWavePhase"
    )
    val secondaryWavePhase by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondaryWavePhase"
    )
    val baseColor = if (progress <= 0.5f) {
        Color(0xFF2F6BDB)
    } else {
        Color.White
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val scaledFigurePath = figurePath.copy().apply {
            transform(
                Matrix().apply {
                    scale(w / figureSpec.viewportWidth, h / figureSpec.viewportHeight)
                }
            )
        }
        val waterHeight = h * animatedProgress
        val waterTop = h - waterHeight

        drawPath(
            path = scaledFigurePath,
            brush = Brush.verticalGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0.88f))
            )
        )

        clipPath(scaledFigurePath) {
            drawPath(
                path = makeWavePath(
                    width = w,
                    height = h,
                    waterTop = waterTop,
                    phase = primaryWavePhase,
                    amplitude = 7.dp.toPx(),
                    frequency = 1.2f
                ),
                brush = Brush.verticalGradient(
                    colors = listOf(waterColor, waterColor.copy(alpha = 0.82f)),
                    startY = waterTop,
                    endY = h
                )
            )
            drawPath(
                path = makeWavePath(
                    width = w,
                    height = h,
                    waterTop = waterTop,
                    phase = secondaryWavePhase,
                    amplitude = 5.dp.toPx(),
                    frequency = 1.8f
                ),
                brush = Brush.verticalGradient(
                    colors = listOf(
                        waterColor.copy(alpha = 0.72f),
                        waterColor.copy(alpha = 0.52f)
                    ),
                    startY = waterTop,
                    endY = h
                )
            )
        }
    }
}

private fun makeWavePath(
    width: Float,
    height: Float,
    waterTop: Float,
    phase: Float,
    amplitude: Float,
    frequency: Float
): Path {
    val boundedAmplitude = min(amplitude, min(waterTop, height - waterTop)).coerceAtLeast(0f)
    return Path().apply {
        moveTo(0f, height)
        lineTo(0f, waterTop)
        var x = 0f
        while (x <= width) {
            val angle = ((x / width) * frequency * PI * 2.0) + (phase * PI / 180.0)
            val y = waterTop + boundedAmplitude * sin(angle).toFloat()
            lineTo(x, y)
            x += 3f
        }
        lineTo(width, height)
        close()
    }
}

private data class VectorPathSpec(
    val viewportWidth: Float,
    val viewportHeight: Float,
    val paths: List<String>
)

private fun android.content.res.Resources.readVectorPathSpec(@DrawableRes resId: Int): VectorPathSpec {
    val parser = getXml(resId)
    var viewportWidth = 0f
    var viewportHeight = 0f
    val paths = mutableListOf<String>()

    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG) {
            when (parser.name) {
                "vector" -> {
                    viewportWidth = parser.androidFloatAttribute("viewportWidth")
                    viewportHeight = parser.androidFloatAttribute("viewportHeight")
                }
                "path" -> {
                    val pathData = parser.getAttributeValue(ANDROID_NS, "pathData")
                    if (!pathData.isNullOrBlank()) paths += pathData
                }
            }
        }
        parser.next()
    }

    require(viewportWidth > 0f && viewportHeight > 0f) {
        "Vector drawable must define viewportWidth and viewportHeight."
    }
    require(paths.isNotEmpty()) {
        "Vector drawable must contain at least one path."
    }

    return VectorPathSpec(
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        paths = paths
    )
}

private fun XmlPullParser.androidFloatAttribute(name: String): Float {
    return getAttributeValue(ANDROID_NS, name)?.toFloatOrNull() ?: 0f
}

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
