package com.example.carcare.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    secondary = SoftGreen,
    tertiary = OffWhite,
    background = DarkBlue,
    surface = DarkBlue,
    onPrimary = Color.White,
    onSecondary = DarkBlue,
    onBackground = OffWhite,
    onSurface = OffWhite
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    secondary = SoftGreen,
    tertiary = MediumBlue,
    background = LightGray,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DarkBlue,
    onBackground = DarkBlue,
    onSurface = DarkBlue,
    surfaceVariant = OffWhite,
    primaryContainer = MediumBlue,
    onPrimaryContainer = Color.White
)

@Composable
fun CarCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
