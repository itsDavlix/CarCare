package com.example.carcare.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "textAlpha"
    )
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500L) // Mostrar el splash durante 2.5 segundos
        onSplashFinished()
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor,
                        primaryColor.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "CarCare Logo",
                    tint = primaryColor,
                    modifier = Modifier.size(70.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "CarCare",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = onPrimaryColor,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gestión inteligente de tu flota",
                fontSize = 14.sp,
                color = onPrimaryColor.copy(alpha = textAlpha * 0.85f),
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(64.dp))
            CircularProgressIndicator(
                color = onPrimaryColor.copy(alpha = textAlpha),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = "v1.0",
            fontSize = 12.sp,
            color = onPrimaryColor.copy(alpha = textAlpha * 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}