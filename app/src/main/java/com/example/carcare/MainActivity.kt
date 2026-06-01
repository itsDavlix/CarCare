package com.example.carcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
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
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var splashFinished by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf<Role?>(null) }
    var loggedInDriverIdCard by remember { mutableStateOf<String?>(null) }

    val logout: () -> Unit = {
        currentRole = null
        loggedInDriverIdCard = null
    }

    when {
        !splashFinished -> SplashScreen(onSplashFinished = { splashFinished = true })

        currentRole == null -> LoginScreen(onLogin = { role, idCard ->
            currentRole = role
            loggedInDriverIdCard = idCard
        })

        else -> {
            // ViewModels izados al nivel de la Activity: una sola instancia, compartida
            // entre Admin y Conductor, y lista para pasarse a un NavHost más adelante.
            val vehicleViewModel: VehicleViewModel = viewModel()
            val driverViewModel: DriverViewModel = viewModel()
            val maintenanceViewModel: MaintenanceViewModel = viewModel()
            val assignmentViewModel: AssignmentViewModel = viewModel()

            when (currentRole) {
                Role.ADMIN -> AdminScreen(
                    onBack = logout,
                    vehicleViewModel = vehicleViewModel,
                    maintenanceViewModel = maintenanceViewModel,
                    driverViewModel = driverViewModel,
                    assignmentViewModel = assignmentViewModel
                )

                Role.DRIVER -> DriverScreen(
                    driverIdCard = loggedInDriverIdCard ?: "",
                    onBack = logout,
                    vehicleViewModel = vehicleViewModel,
                    driverViewModel = driverViewModel,
                    assignmentViewModel = assignmentViewModel
                )

                null -> Unit // inalcanzable: lo cubre la rama currentRole == null
            }
        }
    }
}