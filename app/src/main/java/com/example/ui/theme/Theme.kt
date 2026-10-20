package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SkyBlueTop,
    onPrimary = Color.White,
    primaryContainer = FrostedCardBg,
    onPrimaryContainer = Color.White,
    secondary = AccuWeatherOrange,
    onSecondary = Color.White,
    surface = FrostedCardBg,
    onSurface = Color.White,
    surfaceVariant = FrostedCardBg,
    onSurfaceVariant = TextWhiteSecondary,
    background = SkyBlueTop,
    onBackground = Color.White
)

@Composable
fun TejasWeatherTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
