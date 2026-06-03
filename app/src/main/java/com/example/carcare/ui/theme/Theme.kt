package com.example.carcare.ui.theme

import android.app.Activity
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

private val LightColorScheme = lightColorScheme(
    primary = Petrol700,
    onPrimary = Color.White,
    primaryContainer = Petrol100,
    onPrimaryContainer = Petrol900,
    secondary = Amber,
    onSecondary = Color(0xFF3A2706),
    secondaryContainer = AmberSoft,
    onSecondaryContainer = AmberDeep,
    tertiary = Petrol800,
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDF1EE),
    onSurfaceVariant = Muted,
    outline = Line,
    error = StatusOutOfService,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Petrol300,
    onPrimary = Petrol900,
    primaryContainer = Petrol800,
    onPrimaryContainer = Petrol100,
    secondary = Amber,
    onSecondary = Color(0xFF3A2706),
    background = Color(0xFF0B1A19),
    onBackground = Color(0xFFE3EDEB),
    surface = Color(0xFF12211F),
    onSurface = Color(0xFFE3EDEB),
    surfaceVariant = Color(0xFF1C2C2A),
    onSurfaceVariant = Color(0xFFA9C0BC),
    outline = Color(0xFF2C3C3A),
    error = StatusOutOfService,
    onError = Color.White,
)

@Composable
fun CarCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Identidad propia: el color dinámico (wallpaper) queda DESACTIVADO por defecto.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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