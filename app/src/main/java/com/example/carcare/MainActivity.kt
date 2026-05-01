package com.example.carcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.carcare.ui.navigation.Screen
import com.example.carcare.ui.screens.*
import com.example.carcare.ui.theme.CarCareTheme
import com.example.carcare.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    private val vehicleViewModel: VehicleViewModel by viewModels {
        VehicleViewModelFactory((application as CarCareApplication).repository)
    }
    private val maintenanceViewModel: MaintenanceViewModel by viewModels {
        MaintenanceViewModelFactory((application as CarCareApplication).repository)
    }
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as CarCareApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarCareTheme {
                CarCareApp(
                    vehicleViewModel,
                    maintenanceViewModel,
                    expenseViewModel
                )
            }
        }
    }
}

@Composable
fun CarCareApp(
    vehicleViewModel: VehicleViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    expenseViewModel: ExpenseViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home
    ) {
        composable<Screen.Home> {
            HomeScreen(
                viewModel = vehicleViewModel,
                onVehicleClick = { id ->
                    navController.navigate(Screen.VehicleDetail(id))
                },
                onAddVehicleClick = {
                    navController.navigate(Screen.AddEditVehicle())
                }
            )
        }

        composable<Screen.VehicleDetail> { backStackEntry ->
            val detail: Screen.VehicleDetail = backStackEntry.toRoute()
            VehicleDetailScreen(
                vehicleViewModel = vehicleViewModel,
                maintenanceViewModel = maintenanceViewModel,
                expenseViewModel = expenseViewModel,
                vehicleId = detail.vehicleId,
                onEditClick = { id ->
                    navController.navigate(Screen.AddEditVehicle(id))
                },
                onDeleteSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewMaintenance = { id ->
                    navController.navigate(Screen.MaintenanceList(id))
                },
                onViewExpenses = { id ->
                    navController.navigate(Screen.ExpenseList(id))
                }
            )
        }

        composable<Screen.AddEditVehicle> { backStackEntry ->
            val addEdit: Screen.AddEditVehicle = backStackEntry.toRoute()
            AddEditVehicleScreen(
                viewModel = vehicleViewModel,
                vehicleId = addEdit.vehicleId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.MaintenanceList> { backStackEntry ->
            val list: Screen.MaintenanceList = backStackEntry.toRoute()
            MaintenanceListScreen(
                viewModel = maintenanceViewModel,
                vehicleId = list.vehicleId,
                onAddMaintenance = { id ->
                    navController.navigate(Screen.AddMaintenance(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.AddMaintenance> { backStackEntry ->
            val add: Screen.AddMaintenance = backStackEntry.toRoute()
            AddMaintenanceScreen(
                viewModel = maintenanceViewModel,
                vehicleId = add.vehicleId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.ExpenseList> { backStackEntry ->
            val list: Screen.ExpenseList = backStackEntry.toRoute()
            ExpenseListScreen(
                viewModel = expenseViewModel,
                vehicleId = list.vehicleId,
                onAddExpense = { id ->
                    navController.navigate(Screen.AddExpense(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.AddExpense> { backStackEntry ->
            val add: Screen.AddExpense = backStackEntry.toRoute()
            AddExpenseScreen(
                viewModel = expenseViewModel,
                vehicleId = add.vehicleId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
