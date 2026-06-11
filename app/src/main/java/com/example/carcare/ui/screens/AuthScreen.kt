package com.example.carcare.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.carcare.data.AuthSession
import com.example.carcare.model.Role
import com.example.carcare.ui.components.CarCareTopBar
import com.example.carcare.ui.components.drawCarCareGauge
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.Ink
import com.example.carcare.ui.theme.Line
import com.example.carcare.ui.theme.Muted
import com.example.carcare.ui.theme.NeedleRed
import com.example.carcare.ui.theme.Paper
import com.example.carcare.ui.theme.Petrol100
import com.example.carcare.ui.theme.Petrol700
import com.example.carcare.ui.theme.Petrol800
import com.example.carcare.ui.theme.Petrol900
import com.example.carcare.ui.viewmodel.AuthViewModel
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Ease-out-quint: arranque veloz, aterrizaje suave (la curva de todos los viajes).
private val EaseOutQuint = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

// ── Geometría del héroe ──
private val GaugeSize = 128.dp
private val LogoBlockHeight = 210.dp        // gauge + espacios + wordmark + tagline (estimado)
private const val LogoEndScale = 0.94f      // escala del bloque al asentarse en el login
private const val SheetLoginFraction = 0.56f

// ── Geometría de aterrizaje = CarCareTopBar real ──
// gauge 38dp · padding 16/14 · gap 12 · wordmark 19sp (= 38sp * 0.5) · subtítulo 11sp
private val TopBarGaugeSize = 38.dp
private val TopBarHPadding = 16.dp
private val TopBarVPadding = 14.dp
private val TopBarGaugeTextGap = 12.dp
private const val WordmarkEndScale = 0.5f
private val SubtitleHeightEstimate = 15.dp

// ── La aguja como estado del motor ──
private const val NeedleIdle = 0.16f        // ralentí: formulario vacío
private const val NeedleStepCedula = 0.20f  // cédula con formato válido
private const val NeedleStepPassword = 0.20f// contraseña ingresada

// Halo de respiración detrás del gauge (Petrol300 de la paleta)
private val HaloColor = Color(0xFF7FB6B0)

private fun mix(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun ramp(v: Float, from: Float, to: Float) = ((v - from) / (to - from)).coerceIn(0f, 1f)

/**
 * Splash + Login + Partida hacia el panel, en UNA sola escena.
 *
 * La aguja del gauge es una microinteracción FUNCIONAL: revoluciona en el splash,
 * baja a ralentí en el login, sube a medida que el formulario se completa
 * (cédula válida, contraseña), barre en loop al verificar, y revoluciona al tope
 * en el éxito. El gauge comunica "qué tan listo estás para arrancar".
 *
 * Fase 1 (intro): el gauge se dibuja centrado (splash) con un halo que respira.
 * Fase 2 (settle): el logo se asienta y la hoja sube; título → tarjeta → pie
 * entran en cascada (fade + rise) en ventanas solapadas del mismo progreso.
 * Fase 3 (depart, tras login OK): la hoja sigue hasta dejar solo la franja del
 * TopBar; gauge y wordmark viajan en trayectoria curva hasta la geometría exacta
 * del CarCareTopBar, y en el último 28% un CarCareTopBar REAL hace crossfade
 * encima → el frame final es idéntico al del panel y el corte es invisible.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoggedIn: (Role, String) -> Unit
) {
    val played = viewModel.introPlayed
    val start = if (played) 1f else 0f

    // Intro del gauge (coreografía del splash)
    val arc = remember { Animatable(start) }
    val ticks = remember { Animatable(start) }
    val textP = remember { Animatable(start) }

    // La aguja: un solo Animatable, un solo dueño por fase (ver efecto unificado)
    val needle = remember { Animatable(if (played) NeedleIdle else 0f) }

    // 0 = splash centrado · 1 = login asentado
    val settle = remember { Animatable(start) }

    // 0 = login · 1 = convertido en panel (TopBar + hoja completa)
    val depart = remember { Animatable(0f) }

    // Shake horizontal de la tarjeta en error
    val shakeX = remember { Animatable(0f) }

    // Halo de respiración: lento, sutil, vivo (se lee solo en la fase de dibujo)
    val haloTransition = rememberInfiniteTransition(label = "halo")
    val haloBreath by haloTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloBreath"
    )

    var loadingMsg by remember { mutableStateOf("Verificando…") }

    // Último error no nulo: mantiene el texto visible mientras se colapsa con animación
    var lastError by remember { mutableStateOf("") }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { lastError = it }
    }

    // Tamaño medido del wordmark a 38sp: ancla los cálculos de aterrizaje en el TopBar
    var wmSize by remember { mutableStateOf(IntSize.Zero) }

    // Login OK: destino guardado; el rev + la partida corren antes de navegar
    var pendingLogin by remember { mutableStateOf<Pair<Role, String>?>(null) }

    // Saludo contextual según la hora del dispositivo
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    val haptics = LocalHapticFeedback.current

    // Pantalla oscura: iconos del sistema en claro mientras se muestra (igual que el TopBar)
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
                // El rev de presentación: la aguja revienta al tope una vez
                delay(650)
                needle.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow))
            }
            launch { delay(750); textP.animateTo(1f, tween(600, easing = EaseOutQuint)) }
            delay(1500)
            settle.animateTo(1f, tween(650, easing = EaseOutQuint))
            viewModel.markIntroPlayed()
            // Al terminar de asentarse, el efecto unificado baja la aguja a ralentí.
        }
    }

    // ── Dueño único de la aguja según la fase ──
    // éxito > verificando > ralentí + completitud del formulario.
    // El cambio de claves cancela la fase anterior: nunca dos corrutinas en disputa.
    val cedulaReady = viewModel.cedulaValidation.isValid
    val passwordReady = viewModel.password.isNotBlank()
    LaunchedEffect(viewModel.isLoading, pendingLogin != null, viewModel.introPlayed, cedulaReady, passwordReady) {
        when {
            pendingLogin != null -> {
                // Rev de éxito: caída breve y al tope con el spring del splash
                needle.animateTo(0.12f, tween(160, easing = FastOutSlowInEasing))
                needle.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow))
            }
            viewModel.isLoading -> {
                while (true) {
                    needle.animateTo(0.2f, tween(550, easing = FastOutSlowInEasing))
                    needle.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
                }
            }
            viewModel.introPlayed -> {
                // El motor refleja qué tan listo está el formulario
                val target = NeedleIdle +
                        (if (cedulaReady) NeedleStepCedula else 0f) +
                        (if (passwordReady) NeedleStepPassword else 0f)
                needle.animateTo(target, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow))
            }
            // Intro corriendo: el efecto de intro es dueño de la aguja.
        }
    }

    // Mensajes que cubren el cold start de Render
    LaunchedEffect(viewModel.isLoading) {
        if (viewModel.isLoading) {
            delay(3500); loadingMsg = "Encendiendo el motor…"
            delay(6000); loadingMsg = "El servidor dormía, ya casi…"
        } else if (pendingLogin == null) {
            loadingMsg = "Verificando…"
        }
    }

    // Partida: háptica de éxito, el rev toma vuelo (140ms) y la escena se
    // transforma en el panel solapada con él; recién al final se navega.
    LaunchedEffect(pendingLogin) {
        val destino = pendingLogin ?: return@LaunchedEffect
        loadingMsg = "¡Listo!"
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        delay(140)
        depart.animateTo(1f, tween(680, easing = EaseOutQuint))
        delay(40)
        onLoggedIn(destino.first, destino.second)
    }

    LaunchedEffect(viewModel.errorNonce) {
        if (viewModel.errorNonce > 0) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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

    // TopBar real que aterriza encima: subtítulo y avatar según el rol que entró
    val pendingRole = pendingLogin?.first
    val landingSubtitle = if (pendingRole == Role.DRIVER) "Mi panel · Conductor" else "Panel de operaciones · Flota"
    val landingAvatar = if (pendingRole == Role.DRIVER) {
        AuthSession.nombre?.trim()?.firstOrNull()?.uppercase() ?: "C"
    } else "A"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Petrol700, Petrol800, Petrol900)))
    ) {
        val insetDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val w = maxWidth
        val h = maxHeight
        val darkZone = h * (1f - SheetLoginFraction)
        val blockTopSplash = (h - LogoBlockHeight) / 2f
        val blockTopLogin = ((darkZone - LogoBlockHeight * LogoEndScale) / 2f)
            .coerceAtLeast(insetDp + 8.dp)

        // Alto estimado del TopBar (inset + padding 14·2 + fila ~41dp). Solo posiciona
        // el borde superior de la hoja: el crossfade al TopBar real absorbe el error.
        val stripHeight = insetDp + TopBarVPadding * 2 + 41.dp

        // ── HOJA: nace abajo (splash) → 56% (login) → cubre hasta la franja (panel) ──
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = stripHeight)
                .fillMaxWidth()
                .height(h - stripHeight)
                .graphicsLayer {
                    val ty0 = (h - stripHeight).toPx()          // splash: oculta bajo el borde
                    val ty1 = (darkZone - stripHeight).toPx()   // login: borde superior al 44%
                    translationY = mix(mix(ty0, ty1, settle.value), 0f, depart.value)
                    val r = 28.dp.toPx() * (1f - ramp(depart.value, 0.15f, 0.85f))
                    shape = RoundedCornerShape(r, r, 0f, 0f)
                    clip = true
                }
                .background(Paper)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - ramp(depart.value, 0f, 0.35f) }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(26.dp))

                // Cascada de entrada: cada bloque toma una ventana solapada del settle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        val r = ramp(settle.value, 0.45f, 0.80f)
                        alpha = r
                        translationY = (1f - r) * 14.dp.toPx()
                    }
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    Text(
                        text = "Iniciá sesión para continuar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
                Spacer(Modifier.height(18.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val r = ramp(settle.value, 0.55f, 0.95f)
                            alpha = r
                            translationY = (1f - r) * 14.dp.toPx()
                            translationX = shakeX.value.dp.toPx()
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Line),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        // Tinte animado del ícono al enfocar: feedback inmediato y silencioso
                        val cedulaInteraction = remember { MutableInteractionSource() }
                        val cedulaFocused by cedulaInteraction.collectIsFocusedAsState()
                        val cedulaIconTint by animateColorAsState(
                            targetValue = if (cedulaFocused) Petrol700 else Muted,
                            animationSpec = tween(200),
                            label = "cedulaIconTint"
                        )

                        val cedulaV = viewModel.cedulaValidation
                        OutlinedTextField(
                            value = viewModel.cedula,
                            onValueChange = viewModel::onCedulaChange,
                            label = { Text("Cédula") },
                            placeholder = { Text("001-150385-0001A") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = cedulaIconTint)
                            },
                            interactionSource = cedulaInteraction,
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

                        val passwordInteraction = remember { MutableInteractionSource() }
                        val passwordFocused by passwordInteraction.collectIsFocusedAsState()
                        val passwordIconTint by animateColorAsState(
                            targetValue = if (passwordFocused) Petrol700 else Muted,
                            animationSpec = tween(200),
                            label = "passwordIconTint"
                        )

                        val passwordV = viewModel.passwordValidation
                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = viewModel::onPasswordChange,
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = passwordIconTint)
                            },
                            interactionSource = passwordInteraction,
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

                        // El error no salta: aparece con fade + expansión animada
                        AnimatedVisibility(
                            visible = viewModel.errorMessage != null,
                            enter = fadeIn(tween(220)) +
                                    expandVertically(tween(260, easing = EaseOutQuint)),
                            exit = fadeOut(tween(150)) +
                                    shrinkVertically(tween(220, easing = EaseOutQuint))
                        ) {
                            Column {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = lastError,
                                    color = NeedleRed,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Botón con feedback táctil: se hunde 3% al presionar
                        val buttonInteraction = remember { MutableInteractionSource() }
                        val buttonPressed by buttonInteraction.collectIsPressedAsState()
                        val buttonScale by animateFloatAsState(
                            targetValue = if (buttonPressed) 0.97f else 1f,
                            animationSpec = tween(120),
                            label = "buttonScale"
                        )

                        Button(
                            onClick = submit,
                            enabled = !viewModel.isLoading && pendingLogin == null,
                            interactionSource = buttonInteraction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Petrol700,
                                disabledContainerColor = Petrol700.copy(alpha = 0.75f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .graphicsLayer {
                                    scaleX = buttonScale
                                    scaleY = buttonScale
                                }
                        ) {
                            AnimatedContent(
                                targetState = viewModel.isLoading || pendingLogin != null,
                                transitionSpec = {
                                    (fadeIn(tween(240, easing = EaseOutQuint)) +
                                            scaleIn(
                                                initialScale = 0.94f,
                                                animationSpec = tween(240, easing = EaseOutQuint)
                                            ))
                                        .togetherWith(fadeOut(tween(110)))
                                },
                                contentAlignment = Alignment.Center,
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
                    color = Muted,
                    modifier = Modifier.graphicsLayer {
                        val r = ramp(settle.value, 0.70f, 1f)
                        alpha = r
                        translationY = (1f - r) * 14.dp.toPx()
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── GAUGE viajero con halo respirando: centro → franja oscura → TopBar.
        // Trayectoria curva: la vertical resuelve antes (0–86%), la horizontal
        // después (14–100%). El halo viaja con él y se apaga en la partida.
        Canvas(
            modifier = Modifier
                .size(GaugeSize)
                .graphicsLayer {
                    val settleV = settle.value
                    val departV = depart.value
                    val half = GaugeSize.toPx() / 2f
                    val yT = ramp(departV, 0f, 0.86f)
                    val xT = ramp(departV, 0.14f, 1f)

                    val s01 = mix(1f, LogoEndScale, settleV)
                    val sc = mix(s01, TopBarGaugeSize.toPx() / GaugeSize.toPx(), departV)
                    scaleX = sc
                    scaleY = sc

                    val cy0 = blockTopSplash.toPx() + half
                    val cy1 = blockTopLogin.toPx() + half * LogoEndScale
                    val colH = wmSize.height * WordmarkEndScale + SubtitleHeightEstimate.toPx()
                    val rowH = maxOf(TopBarGaugeSize.toPx(), colH)
                    val cx2 = (TopBarHPadding + TopBarGaugeSize / 2).toPx()
                    val cy2 = insetDp.toPx() + TopBarVPadding.toPx() + rowH / 2f

                    translationX = mix(w.toPx() / 2f, cx2, xT) - half
                    translationY = mix(mix(cy0, cy1, settleV), cy2, yT) - half
                    alpha = 1f - ramp(departV, 0.72f, 1f)
                }
        ) {
            val haloAlpha = (0.10f + 0.10f * haloBreath) * arc.value *
                    (1f - ramp(depart.value, 0f, 0.5f))
            if (haloAlpha > 0.005f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(HaloColor.copy(alpha = haloAlpha), Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.85f
                    ),
                    radius = size.minDimension * 0.85f,
                    center = center
                )
            }
            drawCarCareGauge(
                arcProgress = arc.value,
                ticksProgress = ticks.value,
                needleProgress = needle.value
            )
        }

        // ── WORDMARK viajero: bajo el gauge → al costado del gauge en el TopBar ──
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { wmSize = it }
                .graphicsLayer {
                    val settleV = settle.value
                    val departV = depart.value
                    val wmH = wmSize.height.toFloat()
                    val wmW = wmSize.width.toFloat()
                    val belowGauge = (GaugeSize + 14.dp).toPx()
                    val yT = ramp(departV, 0f, 0.86f)
                    val xT = ramp(departV, 0.14f, 1f)

                    val sc = mix(mix(1f, LogoEndScale, settleV), WordmarkEndScale, departV)
                    scaleX = sc
                    scaleY = sc

                    val cy0 = blockTopSplash.toPx() + belowGauge + wmH / 2f
                    val cy1 = blockTopLogin.toPx() + (belowGauge + wmH / 2f) * LogoEndScale
                    val colH = wmH * WordmarkEndScale + SubtitleHeightEstimate.toPx()
                    val rowH = maxOf(TopBarGaugeSize.toPx(), colH)
                    val cx2 = (TopBarHPadding + TopBarGaugeSize + TopBarGaugeTextGap).toPx() +
                            wmW * WordmarkEndScale / 2f
                    val cy2 = insetDp.toPx() + TopBarVPadding.toPx() +
                            (rowH - colH) / 2f + wmH * WordmarkEndScale / 2f

                    translationX = mix(0f, cx2 - w.toPx() / 2f, xT)
                    translationY = mix(mix(cy0, cy1, settleV), cy2, yT) - wmH / 2f +
                            (1f - textP.value) * 12.dp.toPx()
                    alpha = textP.value * (1f - ramp(departV, 0.72f, 1f))
                }
        ) {
            Row {
                Text("Car", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Care", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Amber)
            }
        }

        // ── TAGLINE: acompaña al bloque y se desvanece apenas arranca la partida ──
        Text(
            text = "Gestión de flota vehicular",
            fontSize = 13.sp,
            color = Petrol100,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    val settleV = settle.value
                    val esc = mix(1f, LogoEndScale, settleV)
                    val blockTop = mix(blockTopSplash.toPx(), blockTopLogin.toPx(), settleV)
                    translationY = blockTop +
                            ((GaugeSize + 14.dp).toPx() + wmSize.height + 4.dp.toPx()) * esc +
                            (1f - textP.value) * 12.dp.toPx()
                    scaleX = esc
                    scaleY = esc
                    alpha = textP.value * (1f - ramp(depart.value, 0f, 0.3f))
                }
        )

        // ── TopBar REAL: aterriza por crossfade en el último 28% de la partida.
        // Es el mismo composable del panel destino → frame final pixel-idéntico.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer { alpha = ramp(depart.value, 0.72f, 1f) }
        ) {
            CarCareTopBar(
                onAvatarClick = {},
                avatarLetter = landingAvatar,
                subtitle = landingSubtitle
            )
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