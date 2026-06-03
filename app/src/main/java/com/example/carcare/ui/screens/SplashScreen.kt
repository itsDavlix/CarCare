package com.example.carcare.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Petrol100
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Geometría del velocímetro (mismos gestos que el prototipo HTML)
private const val TRACK_START = 135f   // arranca abajo-izquierda
private const val TRACK_SWEEP = 270f   // deja un hueco de 90° abajo
private const val ACTIVE_SWEEP = 150f  // sector "en cuidado" (ámbar)
private const val NEEDLE_FROM = 160f
private const val NEEDLE_TO = 300f     // apunta arriba-derecha

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Animaciones independientes y escalonadas (como los animation-delay del HTML)
    val arc = remember { Animatable(0f) }      // dibuja el arco activo
    val ticks = remember { Animatable(0f) }    // aparición de las marcas
    val needle = remember { Animatable(0f) }   // barrido de la aguja (con rebote)
    val textP = remember { Animatable(0f) }    // texto que sube + fade
    val barP = remember { Animatable(0f) }     // barra de carga

    LaunchedEffect(Unit) {
        launch { delay(150); arc.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
        launch { delay(500); ticks.animateTo(1f, tween(500)) }
        launch { delay(800); needle.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow)) }
        launch { delay(850); textP.animateTo(1f, tween(600)) }
        launch { delay(1200); barP.animateTo(1f, tween(1400, easing = FastOutSlowInEasing)) }
        delay(2700)
        onSplashFinished()
    }

    val track = Petrol100.copy(alpha = 0.30f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Petrol700, Petrol900))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // -- Velocímetro animado --
            Canvas(modifier = Modifier.size(128.dp)) {
                val stroke = size.minDimension * 0.072f
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f - stroke
                val box = Offset(cx - r, cy - r)
                val arcSize = Size(r * 2f, r * 2f)

                // 1) Pista de fondo
                drawArc(
                    color = track, startAngle = TRACK_START, sweepAngle = TRACK_SWEEP,
                    useCenter = false, topLeft = box, size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                // 2) Arco activo (ámbar) que se dibuja
                drawArc(
                    color = Amber, startAngle = TRACK_START, sweepAngle = ACTIVE_SWEEP * arc.value,
                    useCenter = false, topLeft = box, size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                // 3) Marcas (5), apareciendo escalonadas
                val tickLen = stroke * 1.15f
                for (i in 0..4) {
                    val a = ((ticks.value - i / 5f) * 3f).coerceIn(0f, 1f)
                    if (a <= 0f) continue
                    val ang = (TRACK_START + TRACK_SWEEP * i / 4f) * (PI / 180f).toFloat()
                    val outer = Offset(cx + r * cos(ang), cy + r * sin(ang))
                    val inner = Offset(cx + (r - tickLen) * cos(ang), cy + (r - tickLen) * sin(ang))
                    drawLine(track.copy(alpha = a * 0.9f), inner, outer, stroke * 0.5f, cap = StrokeCap.Round)
                }

                // 4) Aguja (con rebote) + buje central
                val needleAng = lerp(NEEDLE_FROM, NEEDLE_TO, needle.value) * (PI / 180f).toFloat()
                val tip = Offset(cx + r * 0.6f * cos(needleAng), cy + r * 0.6f * sin(needleAng))
                drawLine(Amber, Offset(cx, cy), tip, stroke * 0.8f, cap = StrokeCap.Round)
                drawCircle(Petrol900, radius = stroke * 1.15f, center = Offset(cx, cy))
                drawCircle(track, radius = stroke * 1.15f, center = Offset(cx, cy), style = Stroke(width = stroke * 0.55f))
            }

            Spacer(Modifier.height(20.dp))

            // -- Wordmark (sube + fade) --
            Row(
                modifier = Modifier
                    .alpha(textP.value)
                    .offset(y = ((1f - textP.value) * 10).dp)
            ) {
                Text("Car", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Care", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Amber)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Control inteligente de tu flota",
                fontSize = 13.sp,
                color = Petrol100.copy(alpha = textP.value),
                modifier = Modifier.offset(y = ((1f - textP.value) * 10).dp)
            )

            Spacer(Modifier.height(40.dp))

            // -- Barra de carga --
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .alpha(textP.value)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barP.value.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(4.dp))
                        .background(Amber)
                )
            }
        }

        Text(
            "v1.0",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = textP.value * 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}