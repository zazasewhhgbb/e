package com.weatherfocus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.weatherfocus.app.data.model.ThemeMode

val SkyBlue = Color(0xFF2F80ED)
val SkyBlueDark = Color(0xFF1B3A6B)
val SkyBlueDeep = Color(0xFF123055)
val SunYellow = Color(0xFFF2C94C)
val SunAmber = Color(0xFFF7A440)
val RainTeal = Color(0xFF56CCF2)

private val LightColors = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E7FF),
    onPrimaryContainer = Color(0xFF0B2A55),
    secondary = SunAmber,
    onSecondary = Color(0xFF3A2400),
    secondaryContainer = Color(0xFFFFE3B0),
    onSecondaryContainer = Color(0xFF4A2E00),
    tertiary = RainTeal,
    background = Color(0xFFF3F7FC),
    onBackground = Color(0xFF10233F),
    surface = Color.White,
    onSurface = Color(0xFF10233F),
    surfaceVariant = Color(0xFFE3ECF7),
    onSurfaceVariant = Color(0xFF3D5876),
    outline = Color(0xFFB7C6DA)
)

private val DarkColors = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1F4A85),
    onPrimaryContainer = Color(0xFFE3EEFF),
    secondary = SunYellow,
    onSecondary = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFF5A4200),
    onSecondaryContainer = Color(0xFFFFE9B8),
    tertiary = RainTeal,
    background = Color(0xFF0E1626),
    onBackground = Color(0xFFEAF1FB),
    surface = Color(0xFF152238),
    onSurface = Color(0xFFEAF1FB),
    surfaceVariant = Color(0xFF203350),
    onSurfaceVariant = Color(0xFFC4D3E8),
    outline = Color(0xFF4A5D7A)
)

@Composable
fun WeatherOnlyTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
