package com.example.carcare.ui.components

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Petrol100
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol800
import com.example.carcare.ui.theme.Petrol900
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Encabezado de marca CarCare (gradiente petróleo, logo, wordmark, subtítulo y avatar).
 * Se mete detrás de la barra de estado y pone los iconos del sistema en claro mientras se muestra.
 *
 * @param onAvatarClick acción del avatar (en Admin: cerrar sesión / volver).
 */
@Composable
fun CarCareTopBar(
    onAvatarClick: () -> Unit = {},
    avatarLetter: String = "A",
    subtitle: String = "Panel de operaciones · Flota"
) {
    // Iconos del sistema (reloj, batería) en claro sobre el petróleo; se restaura al salir.
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            val previous = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = false
            onDispose { controller.isAppearanceLightStatusBars = previous }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Petrol800, Petrol700)))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniGauge(Modifier.size(38.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row {
                    Text("Car", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                    Text("Care", color = Amber, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                }
                Text(subtitle, color = Color(0xFFA9D2CD), fontSize = 11.sp)
            }
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Amber)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(avatarLetter, color = Color(0xFF3A2706), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

/** Logo velocímetro en versión estática y pequeña para el encabezado. */
@Composable
private fun MiniGauge(modifier: Modifier = Modifier) {
    val track = Petrol100.copy(alpha = 0.55f)
    Canvas(modifier) {
        val stroke = size.minDimension * 0.095f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f - stroke
        val box = Offset(cx - r, cy - r)
        val arcSize = Size(r * 2f, r * 2f)
        val rad = (PI / 180f).toFloat()

        drawArc(track, 135f, 270f, false, box, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        drawArc(Amber, 135f, 150f, false, box, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))

        val ang = 300f * rad
        val tip = Offset(cx + r * 0.6f * cos(ang), cy + r * 0.6f * sin(ang))
        drawLine(Amber, Offset(cx, cy), tip, stroke * 0.85f, cap = StrokeCap.Round)
        drawCircle(Petrol900, radius = stroke * 1.1f, center = Offset(cx, cy))
        drawCircle(track, radius = stroke * 1.1f, center = Offset(cx, cy), style = Stroke(stroke * 0.5f))
    }
}