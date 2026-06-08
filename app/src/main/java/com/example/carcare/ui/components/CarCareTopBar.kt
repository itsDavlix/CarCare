package com.example.carcare.ui.components

import android.app.Activity
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol800

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
            CarCareGauge(Modifier.size(38.dp))
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