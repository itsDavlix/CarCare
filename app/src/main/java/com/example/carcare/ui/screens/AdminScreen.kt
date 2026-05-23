package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcare.model.*
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    vehicleViewModel: VehicleViewModel = viewModel(),
    maintenanceViewModel: MaintenanceViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Vehículos", "Mantenimiento", "Asignaciones")
    
    var showVehicleForm by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToShowDetails by remember { mutableStateOf<Vehicle?>(null) }

    var showMaintenanceForm by remember { mutableStateOf(false) }
    var maintenanceToEdit by remember { mutableStateOf<Maintenance?>(null) }

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
            when (selectedTab) {
                0 -> FloatingActionButton(onClick = { 
                    vehicleToEdit = null
                    showVehicleForm = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
                }
                1 -> FloatingActionButton(onClick = { 
                    maintenanceToEdit = null
                    showMaintenanceForm = true 
                }) {
                    Icon(Icons.Default.PostAdd, contentDescription = "Registrar Mantenimiento")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(tabs[selectedTab], style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            when (selectedTab) {
                0 -> VehicleListSection(
                    vehicles = vehicleViewModel.vehicles,
                    onVehicleClick = { vehicleToShowDetails = it },
                    onEdit = { 
                        vehicleToEdit = it
                        showVehicleForm = true
                    },
                    onDelete = { vehicleViewModel.deleteVehicle(it.id) }
                )
                1 -> MaintenanceListSection(
                    maintenances = maintenanceViewModel.maintenances,
                    vehicles = vehicleViewModel.vehicles,
                    onEdit = {
                        maintenanceToEdit = it
                        showMaintenanceForm = true
                    },
                    onDelete = { maintenanceViewModel.deleteMaintenance(it.id) },
                    onStatusChange = { maintenance, status ->
                        maintenanceViewModel.updateStatus(maintenance.id, status)
                    }
                )
                2 -> Text("Gestión de asignaciones de vehículos a conductores...")
            }
        }
    }

    if (showVehicleForm) {
        VehicleFormDialog(
            vehicle = vehicleToEdit,
            onDismiss = { showVehicleForm = false },
            onSave = { vehicle ->
                if (vehicleToEdit == null) {
                    vehicleViewModel.addVehicle(vehicle)
                } else {
                    vehicleViewModel.updateVehicle(vehicle)
                }
                showVehicleForm = false
            }
        )
    }

    if (showMaintenanceForm) {
        MaintenanceFormDialog(
            maintenance = maintenanceToEdit,
            vehicles = vehicleViewModel.vehicles,
            onDismiss = { showMaintenanceForm = false },
            onSave = { maintenance ->
                if (maintenanceToEdit == null) {
                    maintenanceViewModel.addMaintenance(maintenance)
                } else {
                    maintenanceViewModel.updateMaintenance(maintenance)
                }
                showMaintenanceForm = false
            }
        )
    }

    if (vehicleToShowDetails != null) {
        VehicleDetailsDialog(
            vehicle = vehicleToShowDetails!!,
            history = maintenanceViewModel.getHistoryForVehicle(vehicleToShowDetails!!.id),
            onDismiss = { vehicleToShowDetails = null },
            onStatusChange = { newStatus ->
                vehicleViewModel.changeStatus(vehicleToShowDetails!!.id, newStatus)
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
    history: List<Maintenance>,
    onDismiss: () -> Unit,
    onStatusChange: (VehicleStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Vehículo") },
        text = {
            LazyColumn {
                item {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Historial de Mantenimiento:", style = MaterialTheme.typography.titleSmall)
                }
                if (history.isEmpty()) {
                    item { Text("No hay registros.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(history) { maintenance ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("${maintenance.type.label} - ${sdf.format(maintenance.date)}")
                                Text("Estado: ${maintenance.status.label}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun MaintenanceListSection(
    maintenances: List<Maintenance>,
    vehicles: List<Vehicle>,
    onEdit: (Maintenance) -> Unit,
    onDelete: (Maintenance) -> Unit,
    onStatusChange: (Maintenance, MaintenanceStatus) -> Unit
) {
    if (maintenances.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay mantenimientos registrados.")
        }
    } else {
        LazyColumn {
            items(maintenances) { maintenance ->
                val vehicle = vehicles.find { it.id == maintenance.vehicleId }
                MaintenanceItem(
                    maintenance = maintenance,
                    vehicle = vehicle,
                    onEdit = { onEdit(maintenance) },
                    onDelete = { onDelete(maintenance) },
                    onStatusChange = { onStatusChange(maintenance, it) }
                )
            }
        }
    }
}

@Composable
fun MaintenanceItem(
    maintenance: Maintenance,
    vehicle: Vehicle?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (MaintenanceStatus) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = maintenance.type.label, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Vehículo: ${vehicle?.brand} ${vehicle?.model} (${vehicle?.plate ?: "N/A"})")
                    Text(text = "Fecha: ${sdf.format(maintenance.date)}")
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estado: ", style = MaterialTheme.typography.bodySmall)
                MaintenanceStatus.entries.forEach { status ->
                    FilterChip(
                        selected = maintenance.status == status,
                        onClick = { onStatusChange(status) },
                        label = { Text(status.label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
            if (maintenance.nextDate != null || maintenance.nextMileage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val nextDateStr = maintenance.nextDate?.let { sdf.format(it) } ?: "N/A"
                val nextKmStr = maintenance.nextMileage?.let { "$it km" } ?: "N/A"
                Text(
                    text = "Próximo: $nextDateStr o $nextKmStr",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormDialog(
    maintenance: Maintenance?,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onSave: (Maintenance) -> Unit
) {
    var selectedVehicleId by remember { mutableStateOf(maintenance?.vehicleId ?: vehicles.firstOrNull()?.id ?: "") }
    var type by remember { mutableStateOf(maintenance?.type ?: MaintenanceType.PREVENTIVE) }
    var description by remember { mutableStateOf(maintenance?.description ?: "") }
    var responsible by remember { mutableStateOf(maintenance?.responsible ?: "") }
    var currentMileage by remember { mutableStateOf(maintenance?.currentMileage?.toString() ?: "") }
    var nextMileage by remember { mutableStateOf(maintenance?.nextMileage?.toString() ?: "") }
    
    // Simplificación de fechas para este ejemplo
    var dateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy").format(maintenance?.date ?: Date())) }
    var nextDateStr by remember { mutableStateOf(maintenance?.nextDate?.let { SimpleDateFormat("dd/MM/yyyy").format(it) } ?: "") }

    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (maintenance == null) "Registrar Mantenimiento" else "Editar Mantenimiento") },
        text = {
            LazyColumn {
                item {
                    // Selector de Vehículo
                    ExposedDropdownMenuBox(expanded = expandedVehicle, onExpandedChange = { expandedVehicle = !expandedVehicle }) {
                        OutlinedTextField(
                            value = vehicles.find { it.id == selectedVehicleId }?.let { "${it.brand} ${it.model} (${it.plate})" } ?: "Seleccionar Vehículo",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vehículo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicle) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedVehicle, onDismissRequest = { expandedVehicle = false }) {
                            vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = { Text("${vehicle.brand} ${vehicle.model} (${vehicle.plate})") },
                                    onClick = { selectedVehicleId = vehicle.id; expandedVehicle = false }
                                )
                            }
                        }
                    }
                    
                    // Selector de Tipo
                    ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                        OutlinedTextField(
                            value = type.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Mantenimiento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            MaintenanceType.entries.forEach { mType ->
                                DropdownMenuItem(
                                    text = { Text(mType.label) },
                                    onClick = { type = mType; expandedType = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("Fecha (dd/mm/yyyy)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = currentMileage, onValueChange = { currentMileage = it }, label = { Text("Kilometraje Actual") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = responsible, onValueChange = { responsible = it }, label = { Text("Taller/Responsable") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nextDateStr, onValueChange = { nextDateStr = it }, label = { Text("Próxima Fecha (dd/mm/yyyy)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nextMileage, onValueChange = { nextMileage = it }, label = { Text("Próximo Kilometraje") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                onSave(
                    Maintenance(
                        id = maintenance?.id ?: java.util.UUID.randomUUID().toString(),
                        vehicleId = selectedVehicleId,
                        type = type,
                        date = try { sdf.parse(dateStr) } catch (e: Exception) { Date() },
                        currentMileage = currentMileage.toLongOrNull() ?: 0L,
                        description = description,
                        responsible = responsible,
                        nextDate = try { if (nextDateStr.isNotEmpty()) sdf.parse(nextDateStr) else null } catch (e: Exception) { null },
                        nextMileage = nextMileage.toLongOrNull(),
                        status = maintenance?.status ?: MaintenanceStatus.PENDING
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
