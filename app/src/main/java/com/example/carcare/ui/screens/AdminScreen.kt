package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: VehicleViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Vehículos", "Mantenimiento", "Asignaciones")
    
    var showForm by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToShowDetails by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.DirectionsCar, contentDescription = null)
                                1 -> Icon(Icons.Default.Build, contentDescription = null)
                                else -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { 
                    vehicleToEdit = null
                    showForm = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(tabs[selectedTab], style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            when (selectedTab) {
                0 -> VehicleListSection(
                    vehicles = viewModel.vehicles,
                    onVehicleClick = { vehicleToShowDetails = it },
                    onEdit = { 
                        vehicleToEdit = it
                        showForm = true
                    },
                    onDelete = { viewModel.deleteVehicle(it.id) }
                )
                1 -> Text("Lista de mantenimientos pendientes y realizados...")
                2 -> Text("Gestión de asignaciones de vehículos a conductores...")
            }
        }
    }

    if (showForm) {
        VehicleFormDialog(
            vehicle = vehicleToEdit,
            onDismiss = { showForm = false },
            onSave = { vehicle ->
                if (vehicleToEdit == null) {
                    viewModel.addVehicle(vehicle)
                } else {
                    viewModel.updateVehicle(vehicle)
                }
                showForm = false
            }
        )
    }

    if (vehicleToShowDetails != null) {
        VehicleDetailsDialog(
            vehicle = vehicleToShowDetails!!,
            onDismiss = { vehicleToShowDetails = null },
            onStatusChange = { newStatus ->
                viewModel.changeStatus(vehicleToShowDetails!!.id, newStatus)
                vehicleToShowDetails = vehicleToShowDetails!!.copy(status = newStatus)
            }
        )
    }
}

@Composable
fun VehicleListSection(
    vehicles: List<Vehicle>,
    onVehicleClick: (Vehicle) -> Unit,
    onEdit: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit
) {
    LazyColumn {
        items(vehicles) { vehicle ->
            VehicleItem(
                vehicle = vehicle,
                onClick = { onVehicleClick(vehicle) },
                onEdit = { onEdit(vehicle) },
                onDelete = { onDelete(vehicle) }
            )
        }
    }
}

@Composable
fun VehicleItem(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Placa: ${vehicle.plate} | Código: ${vehicle.unitCode}")
                StatusBadge(status = vehicle.status)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (Vehicle) -> Unit
) {
    var unitCode by remember { mutableStateOf(vehicle?.unitCode ?: "") }
    var brand by remember { mutableStateOf(vehicle?.brand ?: "") }
    var model by remember { mutableStateOf(vehicle?.model ?: "") }
    var year by remember { mutableStateOf(vehicle?.year?.toString() ?: "") }
    var plate by remember { mutableStateOf(vehicle?.plate ?: "") }
    var fuelType by remember { mutableStateOf(vehicle?.fuelType ?: "") }
    var mileage by remember { mutableStateOf(vehicle?.mileage?.toString() ?: "") }
    var vehicleType by remember { mutableStateOf(vehicle?.vehicleType ?: "") }
    var description by remember { mutableStateOf(vehicle?.description ?: "") }
    var status by remember { mutableStateOf(vehicle?.status ?: VehicleStatus.AVAILABLE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle == null) "Agregar Vehículo" else "Editar Vehículo") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(value = unitCode, onValueChange = { unitCode = it }, label = { Text("Código de Unidad") })
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca") })
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo") })
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Año") })
                    OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Placa") })
                    OutlinedTextField(value = fuelType, onValueChange = { fuelType = it }, label = { Text("Tipo de Combustible") })
                    OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Kilometraje") })
                    OutlinedTextField(value = vehicleType, onValueChange = { vehicleType = it }, label = { Text("Tipo de Vehículo") })
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Vehicle(
                        id = vehicle?.id ?: java.util.UUID.randomUUID().toString(),
                        unitCode = unitCode,
                        brand = brand,
                        model = model,
                        year = year.toIntOrNull() ?: 0,
                        plate = plate,
                        fuelType = fuelType,
                        mileage = mileage.toLongOrNull() ?: 0L,
                        vehicleType = vehicleType,
                        status = status,
                        description = description
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun VehicleDetailsDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onStatusChange: (VehicleStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Vehículo") },
        text = {
            Column {
                Text("Código: ${vehicle.unitCode}", style = MaterialTheme.typography.bodyLarge)
                Text("Marca/Modelo: ${vehicle.brand} ${vehicle.model}")
                Text("Placa: ${vehicle.plate}")
                Text("Año: ${vehicle.year}")
                Text("Combustible: ${vehicle.fuelType}")
                Text("Kilometraje: ${vehicle.mileage} km")
                Text("Tipo: ${vehicle.vehicleType}")
                Text("Descripción: ${vehicle.description}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cambiar Estado:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    VehicleStatus.entries.forEach { status ->
                        FilterChip(
                            selected = vehicle.status == status,
                            onClick = { onStatusChange(status) },
                            label = { Text(status.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
