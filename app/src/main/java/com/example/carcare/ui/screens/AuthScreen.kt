package com.example.carcare.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.carcare.model.Role
import com.example.carcare.ui.components.drawCarCareGauge
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Ink
import com.example.carcare.ui.theme.Line
import com.example.carcare.ui.theme.Muted
import com.example.carcare.ui.theme.NeedleRed
import com.example.carcare.ui.theme.Paper
import com.example.carcare.ui.theme.Petrol100
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol900
import com.example.carcare.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Ease-out-quint: arranque veloz, aterrizaje suave. Es la curva del "viaje"
// del logo y de la hoja (Jakub: polish 200-500ms; nada de bounce en layout).
private val EaseOutQuint = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

// Alto estimado del bloque del logo (gauge 128 + espaciados + wordmark + tagline).
// Se usa para centrarlo ópticamente: en el splash respecto a la pantalla completa,
// y al asentarse respecto a la franja oscura que queda sobre la hoja.
private val LogoBlockHeight = 210.dp

// Escala final del logo al asentarse: apenas 6% más chico, conserva presencia.
private const val LogoEndScale = 0.94f

/**
 * Splash + Login en UNA sola escena.
 *
 * Fase 1 (intro): el gauge R1 se dibuja centrado, igual que el splash anterior.
 * Fase 2 (settle): el bloque del logo viaja hacia arriba y se reduce, mientras
 * la hoja blanca del formulario sube desde el borde inferior. Al ser el MISMO
 * composable (no dos destinos de navegación), la transición es genuinamente
 * continua: nada se recrea, solo se mueve.
 *
 * La intro corre una sola vez por proceso (introPlayed): al volver por logout,
 * la pantalla aparece ya asentada.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoggedIn: (Role, String) -> Unit
) {
    val played = viewModel.introPlayed
    val start = if (played) 1f else 0f

    // Intro del gauge (coreografía del splash original, recortada: sin barra de carga)
    val arc = remember { Animatable(start) }
    val ticks = remember { Animatable(start) }
    val needleIn = remember { Animatable(start) }
    val textP = remember { Animatable(start) }

    // 0 = splash centrado · 1 = login asentado
    val settle = remember { Animatable(start) }

    // Aguja en loop durante la verificación (se multiplica con needleIn: sin saltos)
    val needleLoop = remember { Animatable(1f) }

    // Shake horizontal de la tarjeta en error de credenciales
    val shakeX = remember { Animatable(0f) }

    var loadingMsg by remember { mutableStateOf("Verificando…") }

    // Login OK: se guarda el destino y se deja que el gauge remate su animación
    // antes de navegar. Recién al terminar el remate se llama onLoggedIn.
    var pendingLogin by remember { mutableStateOf<Pair<Role, String>?>(null) }

    val focusManager = LocalFocusManager.current
    val submit: () -> Unit = {
        if (pendingLogin == null) {
            focusManager.clearFocus()
            viewModel.login { role, cedula -> pendingLogin = role to cedula }
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.introPlayed) {
            launch { delay(150); arc.animateTo(1f, tween(950, easing = FastOutSlowInEasing)) }
            launch { delay(450); ticks.animateTo(1f, tween(450)) }
            launch {
                delay(650)
                needleIn.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow))
            }
            launch { delay(750); textP.animateTo(1f, tween(500)) }
            delay(1650)
            settle.animateTo(1f, tween(550, easing = EaseOutQuint))
            viewModel.markIntroPlayed()
        }
    }

    // Loading: aguja barriendo en loop + mensajes que cubren el cold start de Render
    LaunchedEffect(viewModel.isLoading) {
        if (viewModel.isLoading) {
            launch {
                delay(3500); loadingMsg = "Encendiendo el motor…"
                delay(6000); loadingMsg = "El servidor dormía, ya casi…"
            }
            while (true) {
                needleLoop.animateTo(0.2f, tween(550, easing = FastOutSlowInEasing))
                needleLoop.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
            }
        } else if (pendingLogin == null) {
            // Solo si NO hay un login exitoso en curso: el remate de éxito
            // es dueño de la aguja y del mensaje hasta que se navega.
            loadingMsg = "Verificando…"
            needleLoop.animateTo(1f, tween(250))
        }
    }

    // Remate de éxito: la aguja cae un instante y revienta hasta el tope con el
    // mismo spring del splash (dampingRatio 0.42, el "arranque"). Un respiro de
    // 150ms para que el ojo registre el tope, y recién entonces se navega.
    LaunchedEffect(pendingLogin) {
        val destino = pendingLogin ?: return@LaunchedEffect
        loadingMsg = "¡Listo!"
        needleLoop.animateTo(0.12f, tween(180, easing = FastOutSlowInEasing))
        needleLoop.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow))
        delay(150)
        onLoggedIn(destino.first, destino.second)
        pendingLogin = null
    }

    LaunchedEffect(viewModel.errorNonce) {
        if (viewModel.errorNonce > 0) {
            shakeX.animateTo(0f, keyframes {
                durationMillis = 300
                0f at 0
                (-10f) at 50
                8f at 110
                (-6f) at 170
                4f at 230
                0f at 300
            })
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Petrol700, Petrol900)))
    ) {
        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val sheetHeight = maxHeight * 0.56f
        val darkZone = maxHeight - sheetHeight
        // Splash: logo centrado en la pantalla. Asentado: centrado en la franja oscura.
        val logoStartY = (maxHeight - LogoBlockHeight) / 2f
        val logoEndY = ((darkZone - LogoBlockHeight * LogoEndScale) / 2f)
            .coerceAtLeast(topInset + 8.dp)

        // ── Bloque del logo: se asienta centrado en la franja oscura, casi a tamaño completo ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, lerp(logoStartY, logoEndY, settle.value).roundToPx()) }
                .graphicsLayer {
                    val s = 1f - (1f - LogoEndScale) * settle.value
                    scaleX = s
                    scaleY = s
                    transformOrigin = TransformOrigin(0.5f, 0f)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(modifier = Modifier.size(128.dp)) {
                drawCarCareGauge(
                    arcProgress = arc.value,
                    ticksProgress = ticks.value,
                    needleProgress = needleIn.value * needleLoop.value
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .alpha(textP.value)
                    .offset(y = ((1f - textP.value) * 10).dp)
            ) {
                Text("Car", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Care", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Amber)
            }
            Text(
                text = "Gestión de flota vehicular",
                fontSize = 13.sp,
                color = Petrol100,
                modifier = Modifier.alpha(textP.value)
            )
        }

        // ── Hoja del login: sube desde el borde inferior con la misma curva ──
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .offset { IntOffset(0, (sheetHeight * (1f - settle.value)).roundToPx()) },
            color = Paper,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "Iniciá sesión para continuar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )
                Text(
                    text = "Tu panel se detecta según tu cuenta",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
                Spacer(Modifier.height(18.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeX.value.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Line),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        val cedulaV = viewModel.cedulaValidation
                        OutlinedTextField(
                            value = viewModel.cedula,
                            onValueChange = viewModel::onCedulaChange,
                            label = { Text("Cédula") },
                            placeholder = { Text("001-150385-0001A") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            visualTransformation = CedulaVisualTransformation,
                            singleLine = true,
                            isError = viewModel.attempted && !cedulaV.isValid,
                            supportingText = if (viewModel.attempted && !cedulaV.isValid) {
                                { Text(cedulaV.errorMessage ?: "") }
                            } else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        val passwordV = viewModel.passwordValidation
                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = viewModel::togglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (viewModel.showPassword) {
                                            Icons.Default.VisibilityOff
                                        } else Icons.Default.Visibility,
                                        contentDescription = if (viewModel.showPassword) {
                                            "Ocultar contraseña"
                                        } else "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (viewModel.showPassword) {
                                VisualTransformation.None
                            } else PasswordVisualTransformation(),
                            singleLine = true,
                            isError = viewModel.attempted && !passwordV.isValid,
                            supportingText = if (viewModel.attempted && !passwordV.isValid) {
                                { Text(passwordV.errorMessage ?: "") }
                            } else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        viewModel.errorMessage?.let { error ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = error,
                                color = NeedleRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = submit,
                            enabled = !viewModel.isLoading && pendingLogin == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Petrol700,
                                disabledContainerColor = Petrol700.copy(alpha = 0.75f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            AnimatedContent(
                                targetState = viewModel.isLoading || pendingLogin != null,
                                label = "loginButtonContent"
                            ) { loading ->
                                if (loading) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.padding(horizontal = 6.dp))
                                        Text(loadingMsg, color = Color.White)
                                    }
                                } else {
                                    Text(
                                        text = "Iniciar sesión",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "¿Problemas para entrar? Pedile acceso al administrador de la flota.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Pinta la cédula con guiones mientras se escribe (001-150385-0001A) sin
 * modificar el valor real (que viaja normalizado: 0011503850001A).
 * Guiones visuales en las posiciones 3 y 10 del texto transformado.
 */
private object CedulaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val builder = StringBuilder()
        raw.forEachIndexed { index, c ->
            if (index == 3 || index == 9) builder.append('-')
            builder.append(c)
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + (if (offset > 3) 1 else 0) + (if (offset > 9) 1 else 0)

            override fun transformedToOriginal(offset: Int): Int =
                offset - (if (offset > 3) 1 else 0) - (if (offset > 10) 1 else 0)
        }
        return TransformedText(AnnotatedString(builder.toString()), mapping)
    }
}