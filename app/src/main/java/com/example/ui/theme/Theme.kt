package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndustrialBlue600,
    onPrimary = Color.White,
    primaryContainer = IndustrialNavy800,
    onPrimaryContainer = Color.White,
    secondary = IndustrialCyan500,
    onSecondary = Color.Black,
    tertiary = SignalSuccessGreen,
    background = IndustrialNavy900,
    onBackground = TextPrimaryLight,
    surface = IndustrialNavy800,
    onSurface = TextPrimaryLight,
    surfaceVariant = IndustrialNavy700,
    onSurfaceVariant = TextSecondaryLight,
    error = SignalErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IndustrialNavy900,
    onPrimary = Color.White,
    primaryContainer = IndustrialBlue700,
    onPrimaryContainer = Color.White,
    secondary = IndustrialBlue600,
    onSecondary = Color.White,
    tertiary = SignalSuccessGreen,
    background = IndustrialBgLight,
    onBackground = TextPrimaryDark,
    surface = IndustrialSurfaceLight,
    onSurface = TextPrimaryDark,
    surfaceVariant = IndustrialSurfaceSubtle,
    onSurfaceVariant = TextSecondaryDark,
    error = SignalErrorRed,
    onError = Color.White,
    outline = IndustrialBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
