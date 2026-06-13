package com.example.carcare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Sistema tipográfico CarCare — "panel de instrumentos de flota".
 *
 * Dos familias en eje de contraste real:
 *  - Sans (Default) para texto: jerarquía por peso + escala (ratio ~1.25 entre pasos).
 *  - Monospace para "instrumentos": placas, cédulas, kilometrajes y contadores
 *    (sensación de odómetro/tablero). Se expone [InstrumentNumerals] y estilos
 *    [instrumentLarge]/[instrumentMedium]/[instrumentSmall] para reutilizarlos.
 *
 * Antes la escala era plana (solo bodyLarge override → todo Roboto 16sp). Esta define
 * los slots de Material3 con intención, de modo que TODA la app gana jerarquía a la vez.
 */

/** Familia numérica de "instrumento" (odómetro, contadores, identificadores). */
val InstrumentNumerals = FontFamily.Monospace

private val Sans = FontFamily.Default

// Centra la línea para que los títulos grandes no "floten" alto dentro de su caja.
private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

val Typography = Typography(
    // ── Display: reservado para momentos hero (no se abusa). ──
    displayLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Black,
        fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp,
        lineHeightStyle = tightLineHeight
    ),
    displayMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.ExtraBold,
        fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-0.4).sp,
        lineHeightStyle = tightLineHeight
    ),
    displaySmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp,
        lineHeightStyle = tightLineHeight
    ),

    // ── Headline: títulos de sección/diálogo. ──
    headlineLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.2).sp,
        lineHeightStyle = tightLineHeight
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp,
        lineHeightStyle = tightLineHeight
    ),
    headlineSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, lineHeight = 25.sp, letterSpacing = (-0.1).sp,
        lineHeightStyle = tightLineHeight
    ),

    // ── Title: encabezados de tarjeta / fila. ──
    titleLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),

    // ── Body. ──
    bodyLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp, lineHeight = 17.sp, letterSpacing = 0.2.sp
    ),

    // ── Label: chips, badges, botones, navegación. ──
    labelLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp
    )
)

/* ── Estilos de "instrumento": números/identificadores en monospace. ── */

val instrumentLarge = TextStyle(
    fontFamily = InstrumentNumerals, fontWeight = FontWeight.ExtraBold,
    fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp
)

val instrumentMedium = TextStyle(
    fontFamily = InstrumentNumerals, fontWeight = FontWeight.Bold,
    fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp
)

val instrumentSmall = TextStyle(
    fontFamily = InstrumentNumerals, fontWeight = FontWeight.Medium,
    fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
)
