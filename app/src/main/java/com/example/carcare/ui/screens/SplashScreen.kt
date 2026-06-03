package com.example.carcare.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carcare.R
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol900
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var start by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (start) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 350),
        label = "textAlpha"
    )

    LaunchedEffect(Unit) {
        start = true
        delay(2500L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Petrol700, Petrol900))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_carcare_logo),
                contentDescription = "CarCare",
                modifier = Modifier
                    .size(128.dp)
                    .scale(logoScale)
            )
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.alpha(textAlpha)) {
                Text("Car", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.ui.graphics.Color.White)
                Text("Care", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = Amber)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Control inteligente de tu flota",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = textAlpha)
            )
        }
        Text(
            "v1.0",
            fontSize = 12.sp,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = textAlpha * 0.6f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        )
    }
}