package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.VehicleStatus
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    driverIdCard: String,
    onBack: () -> Unit,
    vehicleViewModel: VehicleViewModel = viewModel(),
    driverViewModel: DriverViewModel = viewModel(),
    assignmentViewModel: AssignmentViewModel = viewModel()
) {
    // Buscar al conductor por cédula
    val driver = remember(driverIdCard, driverViewModel.drivers) {
        driverViewModel.drivers.find { it.idCardNumber == driverIdCard }
    }

    // Buscar la asignación activa para este conductor
    val activeAssignment = remember(driver, assignmentViewModel.assignments) {
        driver?.let { d ->
            assignmentViewModel.assignments.find { it.driverId == d.id && it.status == AssignmentStatus.ACTIVE }
        }
    }

    // Buscar el vehículo asignado
    val assignedVehicle = remember(activeAssignment, vehicleViewModel.vehicles) {
        activeAssignment?.let { a ->
            vehicleViewModel.vehicles.find { it.id == a.vehicleId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Conductor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (driver != null) {
                Text(text = "Hola, ${driver.firstName}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Tu Vehículo Asignado", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                assignedVehicle?.let { vehicle ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleLarge)
                            Text(text = "Placa: ${vehicle.plate}")
                            Spacer(modifier = Modifier.height(8.dp))
                            StatusBadge(status = vehicle.status)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* Reportar mantenimiento */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reportar Problema / Mantenimiento")
                    }
                } ?: Text("No tienes un vehículo asignado actualmente.")
            } else {
                Text("Error: Conductor no encontrado.")
            }
        }
    }
}
