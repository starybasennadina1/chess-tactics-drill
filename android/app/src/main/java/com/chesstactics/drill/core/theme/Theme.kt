package com.chesstactics.drill.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StudyColors = lightColorScheme(primary = Purple, onPrimary = Cream, secondary = Lime, onSecondary = Ink, tertiary = Sage, background = Lavender, onBackground = Ink, surface = Cream, onSurface = Ink, surfaceVariant = SurfaceWarm, onSurfaceVariant = Ink.copy(alpha = 0.72f), outline = Plum, error = Purple)

@Composable
fun AppTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = StudyColors, typography = AppTypography, content = content) }
