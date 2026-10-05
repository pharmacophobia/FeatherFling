package com.stealthsms.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val StealthBackground = Color(0xFF1E1E2E)
val StealthSurface = Color(0xFF252538)
val StealthSurfaceVariant = Color(0xFF181825)
val StealthPrimary = Color(0xFF89B4FA)
val StealthSecondary = Color(0xFFA6E3A1)
val StealthTertiary = Color(0xFFF9E2AF)
val StealthOnBackground = Color(0xFFCDD6F4)
val StealthOnSurface = Color(0xFFCDD6F4)
val StealthOnSurfaceMuted = Color(0xFFA6ADC8)
val StealthError = Color(0xFFF38BA8)
val StealthCardBorder = Color(0xFF313244)

private val DarkColorScheme = darkColorScheme(
    primary = StealthPrimary,
    secondary = StealthSecondary,
    tertiary = StealthTertiary,
    background = StealthBackground,
    surface = StealthSurface,
    surfaceVariant = StealthSurfaceVariant,
    onPrimary = Color(0xFF11111B),
    onSecondary = Color(0xFF11111B),
    onBackground = StealthOnBackground,
    onSurface = StealthOnSurface,
    error = StealthError
)

@Composable
fun StealthSmsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
