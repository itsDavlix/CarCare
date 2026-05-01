package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.data.Vehicle
import com.example.carcare.ui.viewmodel.ExpenseViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleViewModel: VehicleViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    expenseViewModel: ExpenseViewModel,
    vehicleId: Int,
    onEditClick: (Int) -> Unit,
    onDeleteSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onViewMaintenance: (Int) -> Unit,
    onViewExpenses: (Int) -> Unit
) {
    var vehicle by remember { mutableStateOf<Vehicle?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val latestMaintenance by maintenanceViewModel.getLatestMaintenance(vehicleId).collectAsState(initial = null)
    val totalExpenses by expenseViewModel.getTotalExpenses(vehicleId).collectAsState(initial = 0.0)

    LaunchedEffect(vehicleId) {
        vehicle = vehicleViewModel.getVehicleById(vehicleId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Vehículo") },
            text = { Text("¿Estás seguro de que deseas eliminar este vehículo?") },
            confirmButton = {
                TextButton(onClick = {
                    vehicle?.let {
                        vehicleViewModel.deleteVehicle(it)
                        onDeleteSuccess()
                    }
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Vehículo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(vehicleId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        vehicle?.let { v ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vehicle Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Información General", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailItem(label = "Marca", value = v.brand)
                        DetailItem(label = "Modelo", value = v.model)
                        DetailItem(label = "Año", value = v.year.toString())
                        DetailItem(label = "Placa", value = v.plate)
                        DetailItem(label = "Kilometraje", value = "${v.mileage} km")
                    }
                }

                // Latest Maintenance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Último Mantenimiento", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { onViewMaintenance(v.id) }) { Text("Ver todos") }
                        }
                        if (latestMaintenance != null) {
                            Text(text = "Tipo: ${latestMaintenance!!.type}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(text = "Fecha: ${latestMaintenance!!.date}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Costo: $${String.format(Locale.getDefault(), "%.2f", latestMaintenance!!.cost)}", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(text = "No hay mantenimientos registrados", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Expenses Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Gastos", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { onViewExpenses(v.id) }) { Text("Ver todos") }
                        }
                        Text(
                            text = "Total de gastos: $${String.format(Locale.getDefault(), "%.2f", totalExpenses ?: 0.0)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // More Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Más Detalles", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailItem(label = "Motor", value = v.engine)
                        DetailItem(label = "Combustible", value = v.fuelType)
                        DetailItem(label = "Color", value = v.color)
                        DetailItem(label = "Descripción", value = v.description)
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
