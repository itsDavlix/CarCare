package com.example.carcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carcare.data.network.ApiClient
import com.example.carcare.model.Role
import com.example.carcare.ui.screens.AdminScreen
import com.example.carcare.ui.screens.AuthScreen
import com.example.carcare.ui.screens.DriverScreen
import com.example.carcare.ui.theme.CarCareTheme
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.AuthViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ApiClient.warmUp()   // despierta el servidor cuanto antes (cold start de Render)
        setContent {
            CarCareTheme {
                CarCareNavHost()
            }
        }
    }
}

/** Rutas de navegación de la app. */
private object Routes {
    const val AUTH = "auth"
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
    // Además, re-login o cambiar de panel es instantáneo: no vuelve a recargar.
    val vehicleViewModel: VehicleViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()
    val maintenanceViewModel: MaintenanceViewModel = viewModel()
    val assignmentViewModel: AssignmentViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // Cerrar sesión: limpia el token y vuelve al login (sin repetir la intro).
    val logout: () -> Unit = {
        authViewModel.onLoggedOut()
        navController.popBackStack(Routes.AUTH, inclusive = false)
    }

    NavHost(navController = navController, startDestination = Routes.AUTH) {

        composable(Routes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onLoggedIn = { role, cedula ->
                    when (role) {
                        Role.ADMIN -> navController.navigate(Routes.ADMIN)
                        Role.DRIVER -> navController.navigate(Routes.driver(cedula))
                    }
                }
            )
        }

        composable(Routes.ADMIN) {
            AdminScreen(
                onBack = logout,
                vehicleViewModel = vehicleViewModel,
                maintenanceViewModel = maintenanceViewModel,
                driverViewModel = driverViewModel,
                assignmentViewModel = assignmentViewModel
            )
        }

        composable(
            route = Routes.DRIVER,
            arguments = listOf(navArgument("driverIdCard") { type = NavType.StringType })
        ) { backStackEntry ->
            val driverIdCard = backStackEntry.arguments?.getString("driverIdCard").orEmpty()

            DriverScreen(
                driverIdCard = driverIdCard,
                onBack = logout,
                vehicleViewModel = vehicleViewModel,
                driverViewModel = driverViewModel,
                assignmentViewModel = assignmentViewModel,
                maintenanceViewModel = maintenanceViewModel
            )
        }
    }
}