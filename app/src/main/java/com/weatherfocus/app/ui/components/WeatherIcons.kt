package com.weatherfocus.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.weatherfocus.app.data.model.ConditionGroup
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Glossy "3D toy" style weather icons - soft shaded spheres/clouds with a specular highlight and a
 * gentle drop shadow underneath, in the spirit of premium 3D emoji packs, drawn live on a Canvas
 * (no external image/gif assets needed) and lightly animated so they feel alive.
 */
@Composable
fun PremiumWeatherIcon(group: ConditionGroup, size: Dp = 56.dp, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "weatherIcon3d")
    // 0f -> 1f, loops forever - drives spin, falling drops/flakes, drifting fog, lightning flicker.
    val loop by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "loop"
    )
    // -1f <-> 1f, eases back and forth - drives the gentle bob/breathe.
    val bob by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "bob"
    )

    Canvas(modifier = modifier.size(size)) {
        drawGroundShadow()
        when (group) {
            ConditionGroup.CLEAR -> drawSun3D(loop, bob)
            ConditionGroup.CLOUDY -> drawCloud3D(bob, light = Color(0xFFFDFEFF), mid = Color(0xFFD7DEE8), dark = Color(0xFFA6B2C2))
            ConditionGroup.RAIN -> {
                drawCloud3D(bob, light = Color(0xFFF3F6FA), mid = Color(0xFFC3CCD8), dark = Color(0xFF8B96A6))
                drawRainDrops3D(loop)
            }
            ConditionGroup.SNOW -> {
                drawCloud3D(bob, light = Color(0xFFFAFCFF), mid = Color(0xFFDCE6F0), dark = Color(0xFFAEC0D4))
                drawSnowflakes3D(loop)
            }
            ConditionGroup.THUNDER -> {
                drawCloud3D(bob, light = Color(0xFF8B8299), mid = Color(0xFF564E6E), dark = Color(0xFF322B47))
                drawBolt3D(loop)
            }
            ConditionGroup.FOG -> drawFog3D(loop)
            ConditionGroup.UNKNOWN -> drawCloud3D(bob, light = Color(0xFFF1F2F4), mid = Color(0xFFCBCFD6), dark = Color(0xFF9AA1AC))
        }
    }
}

/** A soft, flattened shadow ellipse grounding the icon - every glossy-3D icon set has one under the object. */
private fun DrawScope.drawGroundShadow() {
    val w = size.width
    val h = size.height
    drawOval(
        color = Color.Black.copy(alpha = 0.16f),
        topLeft = Offset(w * 0.22f, h * 0.88f),
        size = Size(w * 0.56f, h * 0.10f)
    )
}

/** A shared "glossy sphere" gradient: light source top-left, deepening to a rich shadow tone bottom-right. */
private fun sphereBrush(w: Float, h: Float, light: Color, mid: Color, dark: Color): Brush = Brush.radialGradient(
    colors = listOf(light, mid, dark),
    center = Offset(w * 0.38f, h * 0.34f),
    radius = w * 0.85f
)

private fun DrawScope.drawSpecularHighlight(center: Offset, r: Float) {
    drawOval(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.62f),
        size = Size(r * 0.5f, r * 0.32f)
    )
}

private fun DrawScope.drawSun3D(loop: Float, bob: Float) {
    val cx = size.width / 2f
    val cy = size.height * 0.46f
    val r = size.minDimension * 0.24f
    val center = Offset(cx, cy)

    // Warm glow that breathes.
    val glowR = r * (2.1f + 0.2f * bob)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFDD7A).copy(alpha = 0.5f), Color.Transparent),
            center = center,
            radius = glowR
        ),
        radius = glowR,
        center = center
    )

    // Rays: chunky rounded rays, slowly spinning, filled with a warm gradient for a glossy feel.
    val rayBrush = Brush.linearGradient(colors = listOf(Color(0xFFFFD24C), Color(0xFFFF9E2C)))
    rotate(degrees = loop * 360f, pivot = center) {
        for (i in 0 until 8) {
            rotate(degrees = i * 45f, pivot = center) {
                val path = Path().apply {
                    val rayLen = r * 0.62f
                    val rayW = r * 0.16f
                    moveTo(cx - rayW / 2f, cy - r * 1.18f)
                    lineTo(cx + rayW / 2f, cy - r * 1.18f)
                    lineTo(cx, cy - r * 1.18f - rayLen)
                    close()
                }
                drawPath(path, brush = rayBrush)
            }
        }
    }

    // Glossy sun sphere.
    drawCircle(
        brush = sphereBrush(size.width, size.height, Color(0xFFFFF3B0), Color(0xFFFFC547), Color(0xFFE8890A)),
        radius = r,
        center = center
    )
    drawSpecularHighlight(center, r)
}

/** A puffy, rounded cloud made of overlapping glossy circles + a base pill, shaded like a soft 3D toy. */
private fun DrawScope.drawCloud3D(bob: Float, light: Color, mid: Color, dark: Color) {
    val w = size.width
    val h = size.height
    val dx = bob * w * 0.03f
    val brush = sphereBrush(w, h, light, mid, dark)

    drawRoundRect(
        brush = brush,
        topLeft = Offset(w * 0.16f + dx, h * 0.42f),
        size = Size(w * 0.68f, h * 0.32f),
        cornerRadius = CornerRadius(h * 0.16f)
    )
    drawCircle(brush = brush, radius = w * 0.19f, center = Offset(w * 0.32f + dx, h * 0.45f))
    drawCircle(brush = brush, radius = w * 0.25f, center = Offset(w * 0.53f + dx, h * 0.38f))
    drawCircle(brush = brush, radius = w * 0.18f, center = Offset(w * 0.70f + dx, h * 0.47f))

    drawSpecularHighlight(Offset(w * 0.42f + dx, h * 0.40f), w * 0.30f)
}

private fun DrawScope.drawRainDrops3D(loop: Float) {
    val w = size.width
    val h = size.height
    for (i in 0 until 3) {
        val phase = (loop + i * 0.33f) % 1f
        val y0 = h * 0.72f
        val y1 = h * 0.98f
        val y = y0 + (y1 - y0) * phase
        val alpha = (1f - phase).coerceIn(0.15f, 1f)
        val x = w * (0.32f + i * 0.19f)
        val dropW = w * 0.075f
        val path = Path().apply {
            moveTo(x, y - dropW * 0.9f)
            quadraticBezierTo(x + dropW * 0.55f, y - dropW * 0.15f, x, y + dropW * 0.55f)
            quadraticBezierTo(x - dropW * 0.55f, y - dropW * 0.15f, x, y - dropW * 0.9f)
            close()
        }
        drawPath(
            path,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFAEE0FF).copy(alpha = alpha), Color(0xFF3E8FD6).copy(alpha = alpha))
            )
        )
    }
}

private fun DrawScope.drawSnowflakes3D(loop: Float) {
    val w = size.width
    val h = size.height
    for (i in 0 until 3) {
        val phase = (loop + i * 0.33f) % 1f
        val y = h * 0.70f + h * 0.26f * phase
        val sway = sin((phase * 2 * PI) + i).toFloat() * w * 0.05f
        val x = w * (0.32f + i * 0.19f) + sway
        val alpha = (1f - phase * 0.7f).coerceIn(0.2f, 1f)
        val r = w * 0.045f
        // Six-point sparkle flake.
        for (a in 0 until 3) {
            val angle = Math.toRadians((a * 60).toDouble())
            val dx = (cos(angle) * r).toFloat()
            val dy = (sin(angle) * r).toFloat()
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(x - dx, y - dy),
                end = Offset(x + dx, y + dy),
                strokeWidth = w * 0.018f,
                cap = StrokeCap.Round
            )
        }
        drawCircle(color = Color.White.copy(alpha = alpha), radius = r * 0.28f, center = Offset(x, y))
    }
}

private fun DrawScope.drawBolt3D(loop: Float) {
    val w = size.width
    val h = size.height
    val flicker = 0.55f + 0.45f * abs(sin(loop * 2 * PI * 1.6)).toFloat()
    val path = Path().apply {
        moveTo(w * 0.57f, h * 0.58f)
        lineTo(w * 0.43f, h * 0.80f)
        lineTo(w * 0.51f, h * 0.80f)
        lineTo(w * 0.41f, h * 1.02f)
        lineTo(w * 0.61f, h * 0.75f)
        lineTo(w * 0.52f, h * 0.75f)
        close()
    }
    // Soft outer glow: same path, larger, low alpha.
    androidx.compose.ui.graphics.drawscope.scale(1.4f, pivot = Offset(w * 0.5f, h * 0.8f)) {
        drawPath(path, color = Color(0xFFFFE45E).copy(alpha = flicker * 0.25f))
    }
    drawPath(
        path,
        brush = Brush.linearGradient(colors = listOf(Color(0xFFFFF3A0).copy(alpha = flicker), Color(0xFFFFB020).copy(alpha = flicker)))
    )
}

private fun DrawScope.drawFog3D(loop: Float) {
    val w = size.width
    val h = size.height
    val tones = listOf(
        Color(0xFFDCE3EA) to Color(0xFFAAB6C4),
        Color(0xFFCBD5DF) to Color(0xFF95A3B4),
        Color(0xFFB9C6D2) to Color(0xFF7F8FA2)
    )
    for (i in 0 until 3) {
        val yy = h * (0.36f + i * 0.19f)
        val amp = w * 0.07f
        val xOff = sin((loop * 2 * PI).toFloat() + i * 1.4f) * amp
        val (light, dark) = tones[i]
        drawRoundRect(
            brush = Brush.horizontalGradient(colors = listOf(light, dark, light)),
            topLeft = Offset(w * 0.14f + xOff, yy),
            size = Size(w * 0.66f, h * 0.11f),
            cornerRadius = CornerRadius(h * 0.055f),
            alpha = 0.9f - i * 0.08f
        )
    }
}
