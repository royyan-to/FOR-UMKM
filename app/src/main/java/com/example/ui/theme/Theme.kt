package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SwissAccent,
    onPrimary = Color.White,
    primaryContainer = SwissAccentMuted,
    onPrimaryContainer = SwissPrimaryText,
    secondary = SwissPrimaryText,
    onSecondary = Color.White,
    secondaryContainer = SwissSurfaceVariant,
    onSecondaryContainer = SwissPrimaryText,
    tertiary = SwissSecondaryText,
    onTertiary = Color.White,
    background = SwissBg,
    onBackground = SwissPrimaryText,
    surface = SwissSurface,
    onSurface = SwissPrimaryText,
    surfaceVariant = SwissSurfaceVariant,
    onSurfaceVariant = SwissSecondaryText,
    outline = SwissBorder,
    outlineVariant = SwissBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
