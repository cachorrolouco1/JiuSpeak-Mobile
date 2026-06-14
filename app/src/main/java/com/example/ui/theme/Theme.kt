package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = NeonCyan,
    tertiary = GoldAccent,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = FontPrimary,
    onSurface = FontPrimary,
    error = Color(0xFFEF4444)
)

private val LightColorScheme = DarkColorScheme // Always premium dark mode as per brand guidelines

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode as default
    dynamicColor: Boolean = false, // Disable dynamic colors to maintain strict dark blue cinematic atmosphere
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
