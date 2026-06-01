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
import com.example.carcare.model.Role
import com.example.carcare.ui.screens.AdminScreen
import com.example.carcare.ui.screens.DriverScreen
import com.example.carcare.ui.screens.LoginScreen
import com.example.carcare.ui.screens.SplashScreen
import com.example.carcare.ui.theme.CarCareTheme
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarCareTheme {
                CarCareNavHost()
            }
        }
    }
}

/** Rutas de navegación de la app. */
private object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val ADMIN = "admin"
    const val DRIVER = "driver/{driverIdCard}"
    fun driver(idCard: String) = "driver/$idCard"
}

@Composable
fun CarCareNavHost() {
    val navController = rememberNavController()

    // Volver al login limpiando admin/conductor del historial.
    val logout: () -> Unit = {
        navController.popBackStack(Routes.LOGIN, inclusive = false)
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { role, idCard ->
                    when (role) {
                        Role.ADMIN -> navController.navigate(Routes.ADMIN)
                        Role.DRIVER -> navController.navigate(Routes.driver(idCard.orEmpty()))
                    }
                }
            )
        }

        composable(Routes.ADMIN) {
            // ViewModels con alcance a esta pantalla; se comparten dentro del panel admin.
            val vehicleViewModel: VehicleViewModel = viewModel()
            val driverViewModel: DriverViewModel = viewModel()
            val maintenanceViewModel: MaintenanceViewModel = viewModel()
            val assignmentViewModel: AssignmentViewModel = viewModel()

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

            val vehicleViewModel: VehicleViewModel = viewModel()
            val driverViewModel: DriverViewModel = viewModel()
            val assignmentViewModel: AssignmentViewModel = viewModel()

            DriverScreen(
                driverIdCard = driverIdCard,
                onBack = logout,
                vehicleViewModel = vehicleViewModel,
                driverViewModel = driverViewModel,
                assignmentViewModel = assignmentViewModel
            )
        }
    }
}