package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    onBack: () -> Unit,
    vehicleViewModel: VehicleViewModel = viewModel()
) {
    // Para propósitos de esta optimización, simplemente tomamos el primer vehículo
    // que esté "En Uso" o asignado, o el primero de la lista si no hay ninguno.
    val assignedVehicle = vehicleViewModel.vehicles.firstOrNull { it.status == VehicleStatus.IN_USE || it.status == VehicleStatus.ASSIGNED }
        ?: vehicleViewModel.vehicles.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Conductor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tu Vehículo Asignado", style = MaterialTheme.typography.headlineSmall)
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
            } ?: Text("No tienes un vehículo asignado actualmente.")
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Reportar mantenimiento */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reportar Problema / Mantenimiento")
            }
        }
    }
}
