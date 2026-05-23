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
import com.example.carcare.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(onBack: () -> Unit) {
    val assignedVehicle = Vehicle("1", "ABC-123", "Toyota Hilux", 2022, "Asignado")

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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = assignedVehicle.model, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Placa: ${assignedVehicle.plate}")
                    Text(text = "Estado: ${assignedVehicle.status}")
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
        }
    }
}
