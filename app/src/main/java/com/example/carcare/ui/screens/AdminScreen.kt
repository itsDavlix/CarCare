package com.example.carcare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcare.model.*
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    vehicleViewModel: VehicleViewModel = viewModel(),
    maintenanceViewModel: MaintenanceViewModel = viewModel(),
    driverViewModel: DriverViewModel = viewModel(),
    assignmentViewModel: AssignmentViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Vehículos", "Mantenimiento", "Conductores", "Asignaciones")
    
    var showVehicleForm by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToShowDetails by remember { mutableStateOf<Vehicle?>(null) }

    var showMaintenanceForm by remember { mutableStateOf(false) }
    var maintenanceToEdit by remember { mutableStateOf<Maintenance?>(null) }

    var showDriverForm by remember { mutableStateOf(false) }
    var driverToEdit by remember { mutableStateOf<Driver?>(null) }
    var driverToShowDetails by remember { mutableStateOf<Driver?>(null) }

    var showAssignmentForm by remember { mutableStateOf(false) }
    var assignmentToComplete by remember { mutableStateOf<Assignment?>(null) }

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
                        label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.Dashboard, contentDescription = null)
                                1 -> Icon(Icons.Default.DirectionsCar, contentDescription = null)
                                2 -> Icon(Icons.Default.Build, contentDescription = null)
                                3 -> Icon(Icons.Default.Person, contentDescription = null)
                                else -> Icon(Icons.Default.Assignment, contentDescription = null)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                1 -> FloatingActionButton(onClick = { 
                    vehicleToEdit = null
                    showVehicleForm = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
                }
                2 -> FloatingActionButton(onClick = { 
                    maintenanceToEdit = null
                    showMaintenanceForm = true 
                }) {
                    Icon(Icons.Default.PostAdd, contentDescription = "Registrar Mantenimiento")
                }
                3 -> FloatingActionButton(onClick = { 
                    driverToEdit = null
                    showDriverForm = true 
                }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Conductor")
                }
                4 -> FloatingActionButton(onClick = { 
                    showAssignmentForm = true 
                }) {
                    Icon(Icons.Default.AddHomeWork, contentDescription = "Nueva Asignación")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(tabs[selectedTab], style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            when (selectedTab) {
                0 -> DashboardSection(
                    vehicles = vehicleViewModel.vehicles,
                    drivers = driverViewModel.drivers,
                    maintenances = maintenanceViewModel.maintenances
                )
                1 -> VehicleListSection(
                    vehicles = vehicleViewModel.vehicles,
                    onVehicleClick = { vehicleToShowDetails = it },
                    onEdit = { 
                        vehicleToEdit = it
                        showVehicleForm = true
                    },
                    onDelete = { vehicleViewModel.deleteVehicle(it.id) }
                )
                2 -> MaintenanceListSection(
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
                3 -> DriverListSection(
                    drivers = driverViewModel.drivers,
                    onDriverClick = { driverToShowDetails = it },
                    onEdit = { 
                        driverToEdit = it
                        showDriverForm = true
                    },
                    onDelete = { driverViewModel.deleteDriver(it.id) }
                )
                4 -> AssignmentListSection(
                    assignments = assignmentViewModel.assignments,
                    vehicles = vehicleViewModel.vehicles,
                    drivers = driverViewModel.drivers,
                    onReturn = { assignmentToComplete = it },
                    onDelete = { assignmentViewModel.deleteAssignment(it.id) }
                )
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

    if (showDriverForm) {
        DriverFormDialog(
            driver = driverToEdit,
            onDismiss = { showDriverForm = false },
            onSave = { driver ->
                if (driverToEdit == null) {
                    driverViewModel.addDriver(driver)
                } else {
                    driverViewModel.updateDriver(driver)
                }
                showDriverForm = false
            }
        )
    }

    if (showAssignmentForm) {
        AssignmentFormDialog(
            vehicles = vehicleViewModel.vehicles.filter { it.status == VehicleStatus.AVAILABLE },
            drivers = driverViewModel.drivers.filter { it.status == DriverStatus.ACTIVE },
            onDismiss = { showAssignmentForm = false },
            onSave = { assignment ->
                assignmentViewModel.addAssignment(assignment)
                vehicleViewModel.changeStatus(assignment.vehicleId, VehicleStatus.IN_USE)
                showAssignmentForm = false
            }
        )
    }

    if (assignmentToComplete != null) {
        ReturnVehicleDialog(
            assignment = assignmentToComplete!!,
            onDismiss = { assignmentToComplete = null },
            onSave = { returnDate, finalMileage, observations, nextStatus ->
                assignmentViewModel.completeAssignment(assignmentToComplete!!.id, returnDate, finalMileage, observations)
                vehicleViewModel.updateMileage(assignmentToComplete!!.vehicleId, finalMileage)
                vehicleViewModel.changeStatus(assignmentToComplete!!.vehicleId, nextStatus)
                assignmentToComplete = null
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

    if (driverToShowDetails != null) {
        DriverDetailsDialog(
            driver = driverToShowDetails!!,
            onDismiss = { driverToShowDetails = null },
            onStatusChange = { newStatus ->
                driverViewModel.updateStatus(driverToShowDetails!!.id, newStatus)
                driverToShowDetails = driverToShowDetails!!.copy(status = newStatus)
            }
        )
    }
}

@Composable
fun DashboardSection(
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    maintenances: List<Maintenance>
) {
    val now = Date()
    val soonCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
    val soon = soonCalendar.time

    val stats = listOf(
        StatData("Total Vehículos", vehicles.size.toString(), Icons.Default.DirectionsCar, Color.Gray),
        StatData("Disponibles", vehicles.count { it.status == VehicleStatus.AVAILABLE }.toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50)),
        StatData("En Uso", vehicles.count { it.status == VehicleStatus.IN_USE }.toString(), Icons.Default.LocalShipping, Color(0xFF2196F3)),
        StatData("En Mantenimiento", vehicles.count { it.status == VehicleStatus.MAINTENANCE }.toString(), Icons.Default.Build, Color(0xFFFF9800)),
        StatData("Fuera de Servicio", vehicles.count { it.status == VehicleStatus.OUT_OF_SERVICE }.toString(), Icons.Default.Warning, Color(0xFFF44336)),
        StatData("Conductores Activos", drivers.count { it.status == DriverStatus.ACTIVE }.toString(), Icons.Default.Person, Color(0xFF4CAF50)),
        StatData("Mantenimientos Pend.", maintenances.count { it.status == MaintenanceStatus.PENDING }.toString(), Icons.Default.Schedule, Color(0xFFFF9800))
    )

    val alerts = mutableListOf<String>()
    
    // Alertas de mantenimiento vencido o próximo
    maintenances.filter { it.status == MaintenanceStatus.PENDING }.forEach { m ->
        val vehicle = vehicles.find { it.id == m.vehicleId }
        val vehicleLabel = vehicle?.let { "${it.brand} (${it.plate})" } ?: "Vehículo"
        
        if (m.nextDate != null) {
            if (m.nextDate.before(now)) alerts.add("VENCIDO: Mantenimiento $vehicleLabel")
            else if (m.nextDate.before(soon)) alerts.add("PRÓXIMO: Mantenimiento $vehicleLabel")
        }
        
        if (m.nextMileage != null && vehicle != null && vehicle.mileage >= m.nextMileage) {
            alerts.add("VENCIDO (KM): Mantenimiento $vehicleLabel")
        }
    }

    // Alertas de licencia
    drivers.filter { it.status == DriverStatus.ACTIVE }.forEach { d ->
        if (d.licenseExpiryDate.before(now)) alerts.add("VENCIDO: Licencia de ${d.fullName}")
        else if (d.licenseExpiryDate.before(soon)) alerts.add("PRÓXIMO: Venc. Licencia ${d.fullName}")
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(400.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(stats) { stat ->
                    StatCard(stat)
                }
            }
        }
        if (alerts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Alertas Críticas", style = MaterialTheme.typography.titleMedium, color = Color.Red)
            }
            items(alerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(alert, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

data class StatData(val label: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun StatCard(stat: StatData) {
    Card(
        modifier = Modifier.padding(4.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(stat.icon, contentDescription = null, tint = stat.color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stat.value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(stat.label, style = MaterialTheme.typography.labelSmall)
        }
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
                        id = vehicle?.id ?: UUID.randomUUID().toString(),
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
            TextButton(onClick = { onDismiss() }) { Text("Cerrar") }
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
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
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
    
    var dateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(maintenance?.date ?: Date())) }
    var nextDateStr by remember { mutableStateOf(maintenance?.nextDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "") }

    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (maintenance == null) "Registrar Mantenimiento" else "Editar Mantenimiento") },
        text = {
            LazyColumn {
                item {
                    ExposedDropdownMenuBox(expanded = expandedVehicle, onExpandedChange = { expandedVehicle = !expandedVehicle }) {
                        OutlinedTextField(
                            value = vehicles.find { it.id == selectedVehicleId }?.let { "${it.brand} ${it.model} (${it.plate})" } ?: "Seleccionar Vehículo",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vehículo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicle) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                    
                    ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                        OutlinedTextField(
                            value = type.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Mantenimiento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                onSave(
                    Maintenance(
                        id = maintenance?.id ?: UUID.randomUUID().toString(),
                        vehicleId = selectedVehicleId,
                        type = type,
                        date = try { sdf.parse(dateStr) ?: Date() } catch (e: Exception) { Date() },
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

@Composable
fun DriverListSection(
    drivers: List<Driver>,
    onDriverClick: (Driver) -> Unit,
    onEdit: (Driver) -> Unit,
    onDelete: (Driver) -> Unit
) {
    if (drivers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay conductores registrados.")
        }
    } else {
        LazyColumn {
            items(drivers) { driver ->
                DriverItem(
                    driver = driver,
                    onClick = { onDriverClick(driver) },
                    onEdit = { onEdit(driver) },
                    onDelete = { onDelete(driver) }
                )
            }
        }
    }
}

@Composable
fun DriverItem(
    driver: Driver,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = driver.fullName, style = MaterialTheme.typography.titleMedium)
                Text(text = "ID: ${driver.idCardNumber}", style = MaterialTheme.typography.bodySmall)
                DriverStatusBadge(status = driver.status)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DriverStatusBadge(status: DriverStatus) {
    val color = when (status) {
        DriverStatus.ACTIVE -> Color(0xFF4CAF50)
        DriverStatus.INACTIVE -> Color(0xFF9E9E9E)
        DriverStatus.SUSPENDED -> Color(0xFFF44336)
    }
    Surface(
        color = color.copy(alpha = 0.2f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverFormDialog(
    driver: Driver?,
    onDismiss: () -> Unit,
    onSave: (Driver) -> Unit
) {
    var firstName by remember { mutableStateOf(driver?.firstName ?: "") }
    var lastName by remember { mutableStateOf(driver?.lastName ?: "") }
    var idCardNumber by remember { mutableStateOf(driver?.idCardNumber ?: "") }
    var age by remember { mutableStateOf(driver?.age?.toString() ?: "") }
    var phone by remember { mutableStateOf(driver?.phone ?: "") }
    var licenseNumber by remember { mutableStateOf(driver?.licenseNumber ?: "") }
    var licenseExpiryStr by remember { mutableStateOf(driver?.licenseExpiryDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "") }
    var status by remember { mutableStateOf(driver?.status ?: DriverStatus.ACTIVE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (driver == null) "Agregar Conductor" else "Editar Conductor") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("Fotos (Simulado)", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        PhotoPlaceholder(label = "Foto Perfil", icon = Icons.Default.AddAPhoto)
                        PhotoPlaceholder(label = "Foto Licencia", icon = Icons.Default.CameraAlt)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = idCardNumber, onValueChange = { idCardNumber = it }, label = { Text("Cédula / Identificación") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Edad") }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.weight(2f))
                    }
                    OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("Número de Licencia") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = licenseExpiryStr, onValueChange = { licenseExpiryStr = it }, label = { Text("Vencimiento Licencia (dd/mm/yyyy)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                onSave(
                    Driver(
                        id = driver?.id ?: UUID.randomUUID().toString(),
                        firstName = firstName,
                        lastName = lastName,
                        idCardNumber = idCardNumber,
                        age = age.toIntOrNull() ?: 0,
                        phone = phone,
                        licenseNumber = licenseNumber,
                        licenseExpiryDate = try { sdf.parse(licenseExpiryStr) ?: Date() } catch (e: Exception) { Date() },
                        status = status
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PhotoPlaceholder(label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { /* Abrir camara/galeria */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DriverDetailsDialog(
    driver: Driver,
    onDismiss: () -> Unit,
    onStatusChange: (DriverStatus) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Conductor") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.LightGray))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = driver.fullName, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Cédula: ${driver.idCardNumber}")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Edad: ${driver.age} años")
                Text("Teléfono: ${driver.phone}")
                Text("Licencia: ${driver.licenseNumber}")
                Text("Vencimiento: ${sdf.format(driver.licenseExpiryDate)}")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cambiar Estado:", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DriverStatus.entries.forEach { status ->
                        FilterChip(
                            selected = driver.status == status,
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

@Composable
fun AssignmentListSection(
    assignments: List<Assignment>,
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onReturn: (Assignment) -> Unit,
    onDelete: (Assignment) -> Unit
) {
    if (assignments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay asignaciones activas.")
        }
    } else {
        LazyColumn {
            items(assignments) { assignment ->
                val vehicle = vehicles.find { it.id == assignment.vehicleId }
                val driver = drivers.find { it.id == assignment.driverId }
                AssignmentItem(
                    assignment = assignment,
                    vehicle = vehicle,
                    driver = driver,
                    onReturn = { onReturn(assignment) },
                    onDelete = { onDelete(assignment) }
                )
            }
        }
    }
}

@Composable
fun AssignmentItem(
    assignment: Assignment,
    vehicle: Vehicle?,
    driver: Driver?,
    onReturn: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Vehículo: ${vehicle?.brand} ${vehicle?.model} (${vehicle?.plate})", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Conductor: ${driver?.fullName}")
                    Text(text = "Salida: ${sdf.format(assignment.departureDate)}")
                    Text(text = "Km Inicial: ${assignment.initialMileage}")
                }
                if (assignment.status == AssignmentStatus.ACTIVE) {
                    Button(onClick = onReturn) { Text("Devolver") }
                }
            }
            if (assignment.status == AssignmentStatus.COMPLETED) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Entregado: ${assignment.returnDate?.let { sdf.format(it) }}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Km Final: ${assignment.finalMileage}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Obs: ${assignment.returnObservations}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentFormDialog(
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    var selectedVehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id ?: "") }
    var selectedDriverId by remember { mutableStateOf(drivers.firstOrNull()?.id ?: "") }
    var initialMileage by remember { mutableStateOf(vehicles.find { it.id == selectedVehicleId }?.mileage?.toString() ?: "") }
    var observations by remember { mutableStateOf("") }
    
    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedDriver by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Asignación") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expandedVehicle, onExpandedChange = { expandedVehicle = !expandedVehicle }) {
                    OutlinedTextField(
                        value = vehicles.find { it.id == selectedVehicleId }?.let { "${it.plate} - ${it.model}" } ?: "Seleccionar Vehículo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehículo Disponible") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicle) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedVehicle, onDismissRequest = { expandedVehicle = false }) {
                        vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text("${vehicle.plate} - ${vehicle.model}") },
                                onClick = { 
                                    selectedVehicleId = vehicle.id
                                    initialMileage = vehicle.mileage.toString()
                                    expandedVehicle = false 
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedDriver, onExpandedChange = { expandedDriver = !expandedDriver }) {
                    OutlinedTextField(
                        value = drivers.find { it.id == selectedDriverId }?.fullName ?: "Seleccionar Conductor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conductor Activo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDriver) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedDriver, onDismissRequest = { expandedDriver = false }) {
                        drivers.forEach { driver ->
                            DropdownMenuItem(
                                text = { Text(driver.fullName) },
                                onClick = { selectedDriverId = driver.id; expandedDriver = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = initialMileage, onValueChange = { initialMileage = it }, label = { Text("Kilometraje Inicial") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones de Salida") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(Assignment(
                    vehicleId = selectedVehicleId,
                    driverId = selectedDriverId,
                    initialMileage = initialMileage.toLongOrNull() ?: 0L,
                    departureObservations = observations
                ))
            }) { Text("Asignar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ReturnVehicleDialog(
    assignment: Assignment,
    onDismiss: () -> Unit,
    onSave: (Date, Long, String, VehicleStatus) -> Unit
) {
    var finalMileage by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var nextStatus by remember { mutableStateOf(VehicleStatus.AVAILABLE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Devolución de Vehículo") },
        text = {
            Column {
                Text("Km Inicial: ${assignment.initialMileage}")
                OutlinedTextField(value = finalMileage, onValueChange = { finalMileage = it }, label = { Text("Kilometraje Final") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones de Entrega") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("Estado posterior:", style = MaterialTheme.typography.labelMedium)
                Row {
                    FilterChip(selected = nextStatus == VehicleStatus.AVAILABLE, onClick = { nextStatus = VehicleStatus.AVAILABLE }, label = { Text("Disponible") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = nextStatus == VehicleStatus.MAINTENANCE, onClick = { nextStatus = VehicleStatus.MAINTENANCE }, label = { Text("Mantenimiento") })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(Date(), finalMileage.toLongOrNull() ?: 0L, observations, nextStatus)
            }) { Text("Confirmar Devolución") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
