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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carcare.ui.components.drawCarCareGauge
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Petrol100
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Animaciones independientes y escalonadas
    val arc = remember { Animatable(0f) }      // dibuja el sector activo
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Petrol700, Petrol900))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // -- Velocímetro animado (R1, mismo dibujo que el header) --
            Canvas(modifier = Modifier.size(128.dp)) {
                drawCarCareGauge(
                    arcProgress = arc.value,
                    ticksProgress = ticks.value,
                    needleProgress = needle.value,
                )
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