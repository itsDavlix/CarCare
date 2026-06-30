package com.example.carcare

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carcare.data.AuthSession
import com.example.carcare.data.SessionEvents
import com.example.carcare.data.SessionStore
import com.example.carcare.data.network.ApiClient
import com.example.carcare.model.Role
import com.example.carcare.ui.screens.AdminScreen
import com.example.carcare.ui.screens.AuthScreen
import com.example.carcare.ui.screens.DriverScreen
import com.example.carcare.ui.screens.ForceChangePasswordScreen
import com.example.carcare.ui.theme.CarCareTheme
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.AuthViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.NotificacionViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionStore.init(applicationContext)  // respaldo de sesión para el auto-login
        ApiClient.warmUp()   // despierta el servidor cuanto antes (cold start de Render)
        setContent {
            // Tema según la preferencia del usuario (Sistema/Claro/Oscuro), persistida.
            val themeMode by SessionStore.themeModeFlow().collectAsState(initial = SessionStore.THEME_SYSTEM)
            val dark = when (themeMode) {
                SessionStore.THEME_LIGHT -> false
                SessionStore.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }
            CarCareTheme(darkTheme = dark) {
                CarCareNavHost()
            }
        }
    }

    // Al SALIR de la app (background) se marca el momento: arranca la ventana de 30 min.
    override fun onStop() {
        super.onStop()
        SessionStore.touchLastActive()
    }

    // Al VOLVER: si la sesión guardada caducó por ausencia (>30 min) y seguís logueado, se
    // cierra como un 401 (vuelve al login). En el arranque en frío aún no hay sesión en memoria,
    // así que no dispara nada (el auto-login ya aplica la misma ventana al restaurar).
    override fun onStart() {
        super.onStart()
        if (AuthSession.isLoggedIn) {
            lifecycleScope.launch {
                if (SessionStore.isExpiredByAbsence()) SessionEvents.signalExpired()
            }
        }
    }
}

/** Rutas de navegación de la app. */
private object Routes {
    const val AUTH = "auth"
    const val FORCE_PW = "force-password"
    const val ADMIN = "admin"
    const val DRIVER = "driver/{driverIdCard}"
    fun driver(idCard: String) = "driver/$idCard"
}

@Composable
fun CarCareNavHost() {
    val navController = rememberNavController()

    // ViewModels con alcance de Activity: se crean UNA sola vez al arrancar la app
    // (durante el splash) y se COMPARTEN entre el panel admin y el de conductor.
    // Como `init { load() }` dispara la carga al crearse, los datos empiezan a bajar
    // mientras se ve el splash y el login → al entrar al panel ya suelen estar listos.
    val vehicleViewModel: VehicleViewModel = hiltViewModel()
    val driverViewModel: DriverViewModel = hiltViewModel()
    val maintenanceViewModel: MaintenanceViewModel = hiltViewModel()
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val notificacionViewModel: NotificacionViewModel = hiltViewModel()

    // Destino de auto-login: se setea al arrancar si hay sesión válida en disco. Se ANULA al
    // cerrar sesión / expirar para que AuthScreen no vuelva a auto-loguear (bug: reabría la sesión).
    var autoLoginDest by remember { mutableStateOf<Pair<Role, String>?>(null) }

    // Cerrar sesión: limpia el token, anula el auto-login y vuelve al login (sin repetir la intro).
    val logout: () -> Unit = {
        authViewModel.onLoggedOut()
        autoLoginDest = null
        navController.popBackStack(Routes.AUTH, inclusive = false)
    }

    // Navega al panel según el rol (lo usan el login normal y el post-cambio forzado).
    val goToPanel: (Role, String) -> Unit = { role, cedula ->
        when (role) {
            Role.ADMIN -> navController.navigate(Routes.ADMIN)
            Role.DRIVER -> navController.navigate(Routes.driver(cedula))
        }
    }

    // Auto-login: si hay una sesión válida en disco, restaurarla y PRECARGAR los datos,
    // pero NO navegar de una: se le pasa el destino a AuthScreen para que primero corra
    // la intro y recién entre (con la misma transición que un login real). Si no hay
    // sesión válida, autoLoginDest queda null y se ve el login normal.
    LaunchedEffect(Unit) {
        val s = SessionStore.load() ?: return@LaunchedEffect
        val role = if (s.role == "ADMIN") Role.ADMIN else Role.DRIVER
        AuthSession.restore(s.token, role, s.nombre, s.cedula, s.conductorId, s.debeCambiarPassword)
        // Cargar mientras corre la animación para que los datos estén listos al entrar.
        vehicleViewModel.load()
        driverViewModel.load()
        maintenanceViewModel.load()
        assignmentViewModel.load()
        autoLoginDest = role to s.cedula
    }

    // Sesión expirada (401): el interceptor ya limpió la sesión; acá avisamos y
    // volvemos al login descartando el back stack.
    val context = LocalContext.current
    val sessionExpired by SessionEvents.expired.collectAsState()
    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            authViewModel.onLoggedOut()
            autoLoginDest = null
            navController.navigate(Routes.AUTH) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
            Toast.makeText(context, "Tu sesión expiró. Iniciá sesión de nuevo.", Toast.LENGTH_LONG).show()
            SessionEvents.reset()
        }
    }

    NavHost(navController = navController, startDestination = Routes.AUTH) {

        // El match cut: al salir hacia el panel, AUTH se queda visible (fadeOut a 0.99,
        // imperceptible) mostrando su TopBar real ya aterrizado, mientras el destino
        // hace fadeIn ENCIMA. Como ambos TopBar son el mismo composable con los mismos
        // parámetros, esos píxeles no cambian: solo se materializa el contenido.
        composable(
            route = Routes.AUTH,
            exitTransition = { fadeOut(animationSpec = tween(300), targetAlpha = 0.99f) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) }
        ) {
            AuthScreen(
                viewModel = authViewModel,
                autoLogin = autoLoginDest,
                onLoggedIn = { role, cedula ->
                    // Si la clave es la inicial, primero el cambio obligatorio.
                    if (AuthSession.debeCambiarPassword) {
                        navController.navigate(Routes.FORCE_PW)
                    } else {
                        goToPanel(role, cedula)
                    }
                }
            )
        }

        composable(
            route = Routes.FORCE_PW,
            enterTransition = { fadeIn(animationSpec = tween(300)) }
        ) {
            ForceChangePasswordScreen(
                authViewModel = authViewModel,
                cedula = AuthSession.cedula.orEmpty(),
                onChanged = {
                    val role = AuthSession.role ?: Role.DRIVER
                    val cedula = AuthSession.cedula.orEmpty()
                    AuthSession.markPasswordChanged()
                    navController.navigate(
                        when (role) {
                            Role.ADMIN -> Routes.ADMIN
                            Role.DRIVER -> Routes.driver(cedula)
                        }
                    ) { popUpTo(Routes.FORCE_PW) { inclusive = true } }
                },
                onLogout = logout
            )
        }

        composable(
            route = Routes.ADMIN,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
            AdminScreen(
                onBack = logout,
                vehicleViewModel = vehicleViewModel,
                maintenanceViewModel = maintenanceViewModel,
                driverViewModel = driverViewModel,
                assignmentViewModel = assignmentViewModel,
                notificacionViewModel = notificacionViewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Routes.DRIVER,
            arguments = listOf(navArgument("driverIdCard") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(220)) }
        ) { backStackEntry ->
            val driverIdCard = backStackEntry.arguments?.getString("driverIdCard").orEmpty()

            DriverScreen(
                driverIdCard = driverIdCard,
                onBack = logout,
                vehicleViewModel = vehicleViewModel,
                driverViewModel = driverViewModel,
                assignmentViewModel = assignmentViewModel,
                maintenanceViewModel = maintenanceViewModel,
                authViewModel = authViewModel,
                notificacionViewModel = notificacionViewModel
            )
        }
    }
}