package com.moltrax.personalnoteapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = AppColors.Accent,
    onPrimary        = Color.White,
    primaryContainer = AppColors.AccentGlow,
    background       = AppColors.BgDeep,
    surface          = AppColors.BgSurface,
    surfaceVariant   = AppColors.BgCard,
    onBackground     = AppColors.TextPrimary,
    onSurface        = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary,
    outline          = AppColors.BorderSubtle,
    error            = AppColors.Error,
)

private val LightColorScheme = lightColorScheme(
    primary          = AppColors.Accent,
    onPrimary        = Color.White,
    background       = Color(0xFFF5F5FA),
    surface          = Color.White,
    surfaceVariant   = Color(0xFFEEEEF5),
    onBackground     = Color(0xFF1A1A24),
    onSurface        = Color(0xFF1A1A24),
    onSurfaceVariant = Color(0xFF666678),
    outline          = Color(0xFFDDDDE8),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content     = content,
    )
}
