package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.data.Maintenance
import com.example.carcare.ui.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceListScreen(
    viewModel: MaintenanceViewModel,
    vehicleId: Int,
    onAddMaintenance: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val maintenances by viewModel.getMaintenanceForVehicle(vehicleId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddMaintenance(vehicleId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Maintenance")
            }
        }
    ) { padding ->
        if (maintenances.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "No hay mantenimientos registrados", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(maintenances) { maintenance ->
                    MaintenanceItem(
                        maintenance = maintenance,
                        onDelete = { viewModel.deleteMaintenance(maintenance) }
                    )
                }
            }
        }
    }
}

@Composable
fun MaintenanceItem(maintenance: Maintenance, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = maintenance.type, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Fecha", value = maintenance.date)
            DetailRow(label = "Kilometraje", value = "${maintenance.mileage} km")
            DetailRow(label = "Costo", value = "$${maintenance.cost}")
            DetailRow(label = "Taller", value = maintenance.workshop)
            
            if (maintenance.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = maintenance.description, style = MaterialTheme.typography.bodyMedium)
            }
            
            if (maintenance.nextMileage != null || maintenance.nextDate != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                Text(text = "Próximo Mantenimiento Sugerido", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                maintenance.nextMileage?.let { Text(text = "A los: $it km", style = MaterialTheme.typography.bodySmall) }
                maintenance.nextDate?.let { Text(text = "Fecha aprox: $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
