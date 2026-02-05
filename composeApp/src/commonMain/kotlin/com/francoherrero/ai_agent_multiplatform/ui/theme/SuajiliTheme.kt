package com.francoherrero.ai_agent_multiplatform.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Suajili brand colors (derived from suajili.com.ar)
val SuajiliOrange = Color(0xFFFF6B35)
val SuajiliOrangeDark = Color(0xFFE55A25)
val SuajiliBlue = Color(0xFF1A4B7C)
val SuajiliBlueDark = Color(0xFF0D3A5C)
val SuajiliTeal = Color(0xFF2D8B8B)

private val LightColorScheme = lightColorScheme(
    primary = SuajiliOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4D9),
    onPrimaryContainer = SuajiliOrangeDark,
    secondary = SuajiliBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E4F0),
    onSecondaryContainer = SuajiliBlueDark,
    tertiary = SuajiliTeal,
    onTertiary = Color.White,
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB59D),
    onPrimary = Color(0xFF5E1900),
    primaryContainer = SuajiliOrangeDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFAAC7E8),
    onSecondary = Color(0xFF0D3A5C),
    secondaryContainer = SuajiliBlueDark,
    onSecondaryContainer = Color(0xFFD6E4F0),
    tertiary = Color(0xFF80CFCF),
    onTertiary = Color(0xFF003737),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun SuajiliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
