package com.example.carcare.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.NeedleRed
import com.example.carcare.ui.theme.Petrol100
import com.example.carcare.ui.theme.Petrol300
import com.example.carcare.ui.theme.Petrol900
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Velocímetro de marca CarCare (prototipo R1), con proporciones EXACTAS:
 * - arco abierto 270° (pista teal) + sector activo 155° (ámbar)
 * - 5 marcas DEBAJO del arco (radios 19–24 del prototipo; arco a radio 30)
 * - aguja roja + buje (disco rojo + punto petróleo)
 *
 * Es una extensión de DrawScope para reusarla tal cual en el splash (animado) y el header
 * (estático). Los `*Progress` (0..1) animan; en estático van en 1f. Como todo se deriva del
 * radio del arco, splash y header quedan idénticos a cualquier tamaño.
 */
fun DrawScope.drawCarCareGauge(
    arcProgress: Float = 1f,
    ticksProgress: Float = 1f,
    needleProgress: Float = 1f,
    trackColor: Color = Petrol300,
    markColor: Color = Petrol100,
    activeColor: Color = Amber,
    needleColor: Color = NeedleRed,
    hubInnerColor: Color = Petrol900,
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rad = (PI / 180f).toFloat()

    // Radio del arco (línea media). El grosor sale del ratio del prototipo (6/30 = 0.20)
    // y el radio se elige para llenar el lienzo dejando medio grosor de margen.
    val r = size.minDimension * (0.5f / 1.2f)
    val arcStroke = r * (6f / 30f)

    val box = Offset(cx - r, cy - r)
    val arcSize = Size(r * 2f, r * 2f)

    // 1) Pista (teal): arco abierto de 270° (hueco de 90° abajo)
    drawArc(trackColor, 135f, 270f, false, box, arcSize, style = Stroke(arcStroke, cap = StrokeCap.Round))

    // 2) Sector activo (ámbar): 155°, hasta donde apunta la aguja
    drawArc(activeColor, 135f, 155f * arcProgress, false, box, arcSize, style = Stroke(arcStroke, cap = StrokeCap.Round))

    // 3) Marcas (5) DEBAJO del arco (radios 19–24 del prototipo), apareciendo escalonadas
    val tickInner = r * (19f / 30f)
    val tickOuter = r * (24f / 30f)
    val tickStroke = arcStroke * (2.5f / 6f)
    for (i in 0..4) {
        val a = ((ticksProgress - i * 0.15f) / 0.4f).coerceIn(0f, 1f)
        if (a <= 0f) continue
        val ang = (135f + 270f * i / 4f) * rad
        val inner = Offset(cx + tickInner * cos(ang), cy + tickInner * sin(ang))
        val outer = Offset(cx + tickOuter * cos(ang), cy + tickOuter * sin(ang))
        drawLine(markColor.copy(alpha = a), inner, outer, tickStroke, cap = StrokeCap.Round)
    }

    // 4) Aguja (roja, con barrido) + buje (disco rojo + punto petróleo)
    val needleAng = (160f + (290f - 160f) * needleProgress) * rad
    val needleLen = r * (23f / 30f)
    val tip = Offset(cx + needleLen * cos(needleAng), cy + needleLen * sin(needleAng))
    drawLine(needleColor, Offset(cx, cy), tip, arcStroke * (4.5f / 6f), cap = StrokeCap.Round)
    drawCircle(needleColor, radius = r * (5.5f / 30f), center = Offset(cx, cy))
    drawCircle(hubInnerColor, radius = r * (2.2f / 30f), center = Offset(cx, cy))
}

/** Versión estática lista para usar (header, etc.). */
@Composable
fun CarCareGauge(modifier: Modifier = Modifier) {
    Canvas(modifier) { drawCarCareGauge() }
}