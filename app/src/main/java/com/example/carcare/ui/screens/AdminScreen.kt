package com.example.carcare.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carcare.model.*
import com.example.carcare.ui.components.DatePickerField
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import com.example.carcare.util.ValidationResult
import com.example.carcare.util.Validators
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
                }) { Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo") }
                2 -> FloatingActionButton(onClick = {
                    maintenanceToEdit = null
                    showMaintenanceForm = true
                }) { Icon(Icons.Default.PostAdd, contentDescription = "Registrar Mantenimiento") }
                3 -> FloatingActionButton(onClick = {
                    driverToEdit = null
                    showDriverForm = true
                }) { Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Conductor") }
                4 -> FloatingActionButton(onClick = {
                    showAssignmentForm = true
                }) { Icon(Icons.Default.AddHomeWork, contentDescription = "Nueva Asignación") }
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
                    maintenances = maintenanceViewModel.maintenances,
                    assignments = assignmentViewModel.assignments
                )
                1 -> VehicleListSection(
                    vehicles = vehicleViewModel.vehicles,
                    onVehicleClick = { vehicleToShowDetails = it },
                    onEdit = { vehicleToEdit = it; showVehicleForm = true },
                    onDelete = { vehicle ->
                        val hasHistory = maintenanceViewModel.getHistoryForVehicle(vehicle.id).isNotEmpty() ||
                                assignmentViewModel.assignments.any { it.vehicleId == vehicle.id }
                        if (hasHistory) {
                            // En una app real usaríamos un Snackbar o un diálogo de alerta
                            println("No se puede eliminar un vehículo con historial")
                        } else {
                            vehicleViewModel.deleteVehicle(vehicle.id)
                        }
                    }
                )
                2 -> MaintenanceListSection(
                    maintenances = maintenanceViewModel.maintenances,
                    vehicles = vehicleViewModel.vehicles,
                    onEdit = { maintenanceToEdit = it; showMaintenanceForm = true },
                    onDelete = { maintenanceViewModel.deleteMaintenance(it.id) },
                    onStatusChange = { m, s -> maintenanceViewModel.updateStatus(m.id, s) }
                )
                3 -> DriverListSection(
                    drivers = driverViewModel.drivers,
                    onDriverClick = { driverToShowDetails = it },
                    onEdit = { driverToEdit = it; showDriverForm = true },
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
            existingVehicles = vehicleViewModel.vehicles,
            onDismiss = { showVehicleForm = false },
            onSave = { vehicle ->
                if (vehicleToEdit == null) vehicleViewModel.addVehicle(vehicle)
                else vehicleViewModel.updateVehicle(vehicle)
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
                if (maintenanceToEdit == null) maintenanceViewModel.addMaintenance(maintenance)
                else maintenanceViewModel.updateMaintenance(maintenance)
                showMaintenanceForm = false
            }
        )
    }

    if (showDriverForm) {
        DriverFormDialog(
            driver = driverToEdit,
            existingDrivers = driverViewModel.drivers,
            onDismiss = { showDriverForm = false },
            onSave = { driver ->
                if (driverToEdit == null) driverViewModel.addDriver(driver)
                else driverViewModel.updateDriver(driver)
                showDriverForm = false
            }
        )
    }

    if (showAssignmentForm) {
        val busyDriverIds = assignmentViewModel.assignments
            .filter { it.status == AssignmentStatus.ACTIVE }
            .map { it.driverId }
            .toSet()

        AssignmentFormDialog(
            vehicles = vehicleViewModel.vehicles.filter { it.status == VehicleStatus.AVAILABLE },
            drivers = driverViewModel.drivers.filter {
                it.status == DriverStatus.ACTIVE && it.id !in busyDriverIds
            },
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
            maintenanceHistory = maintenanceViewModel.getHistoryForVehicle(vehicleToShowDetails!!.id),
            assignmentHistory = assignmentViewModel.assignments.filter { it.vehicleId == vehicleToShowDetails!!.id },
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
    maintenances: List<Maintenance>,
    assignments: List<Assignment>
) {
    val now = Date()
    val soon = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time

    val stats = listOf(
        StatData("Total Vehículos", vehicles.size.toString(), Icons.Default.DirectionsCar, Color.Gray),
        StatData("Disponibles", vehicles.count { it.status == VehicleStatus.AVAILABLE }.toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50)),
        StatData("Asignados", vehicles.count { it.status == VehicleStatus.ASSIGNED }.toString(), Icons.Default.AssignmentInd, Color(0xFF9C27B0)),
        StatData("En Uso", vehicles.count { it.status == VehicleStatus.IN_USE }.toString(), Icons.Default.LocalShipping, Color(0xFF2196F3)),
        StatData("En Mantenimiento", vehicles.count { it.status == VehicleStatus.MAINTENANCE }.toString(), Icons.Default.Build, Color(0xFFFF9800)),
        StatData("Fuera de Servicio", vehicles.count { it.status == VehicleStatus.OUT_OF_SERVICE }.toString(), Icons.Default.Warning, Color(0xFFF44336)),
        StatData("Conductores Activos", drivers.count { it.status == DriverStatus.ACTIVE }.toString(), Icons.Default.Person, Color(0xFF4CAF50)),
        StatData("Conductores Inactivos", drivers.count { it.status == DriverStatus.INACTIVE }.toString(), Icons.Default.PersonOff, Color(0xFF9E9E9E)),
        StatData("Mant. Pendientes", maintenances.count { it.status == MaintenanceStatus.PENDING }.toString(), Icons.Default.Schedule, Color(0xFFFF9800)),
        StatData("Asignaciones Activas", assignments.count { it.status == AssignmentStatus.ACTIVE }.toString(), Icons.Default.Assignment, Color(0xFF3F51B5))
    )

    val overdueMaintenances = maintenances.count { it.status == MaintenanceStatus.PENDING && 
            ((it.nextDate != null && it.nextDate.before(now)) || 
             (it.nextMileage != null && vehicles.find { v -> v.id == it.vehicleId }?.let { v -> v.mileage >= it.nextMileage } == true)) 
    }

    val extraStats = listOf(
        StatData("Mant. Vencidos", overdueMaintenances.toString(), Icons.Default.RunningWithErrors, Color.Red),
        StatData("Pend. Revisión", vehicles.count { it.status == VehicleStatus.PENDING_REVIEW }.toString(), Icons.Default.FactCheck, Color(0xFF795548))
    )

    val alerts = mutableListOf<String>()

    maintenances.filter { it.status == MaintenanceStatus.PENDING }.forEach { m ->
        val vehicle = vehicles.find { it.id == m.vehicleId }
        val vehicleLabel = vehicle?.let { "${it.brand} (${Validators.formatPlate(it.plate)})" } ?: "Vehículo"

        if (m.nextDate != null) {
            if (m.nextDate.before(now)) alerts.add("VENCIDO: Mantenimiento $vehicleLabel")
            else if (m.nextDate.before(soon)) alerts.add("PRÓXIMO: Mantenimiento $vehicleLabel")
        }

        if (m.nextMileage != null && vehicle != null && vehicle.mileage >= m.nextMileage) {
            alerts.add("VENCIDO (KM): Mantenimiento $vehicleLabel")
        }
    }

    drivers.filter { it.status == DriverStatus.ACTIVE }.forEach { d ->
        if (d.licenseExpiryDate.before(now)) alerts.add("VENCIDO: Licencia de ${d.fullName}")
        else if (d.licenseExpiryDate.before(soon)) alerts.add("PRÓXIMO: Venc. Licencia ${d.fullName}")
    }

    val lastCheckOuts = assignments.sortedByDescending { it.departureDate }.take(3)
    val lastCheckIns = assignments.filter { it.status == AssignmentStatus.COMPLETED }.sortedByDescending { it.returnDate }.take(3)

    val totalKm = vehicles.sumOf { it.mileage }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Estadísticas de Flota", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(600.dp), // Increased height for more cards
                contentPadding = PaddingValues(4.dp),
                userScrollEnabled = false // Let LazyColumn handle scroll
            ) {
                items(stats + extraStats) { stat -> StatCard(stat) }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen de Uso", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kilometraje total de la flota: $totalKm km", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Distribución de Estados", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Gráfico de barras simple
                    VehicleStatusDistributionChart(vehicles)
                }
            }
        }

        if (lastCheckOuts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Últimas Salidas", style = MaterialTheme.typography.titleMedium)
            }
            items(lastCheckOuts) { assignment ->
                val vehicle = vehicles.find { it.id == assignment.vehicleId }
                val driver = drivers.find { it.id == assignment.driverId }
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ListItem(
                        headlineContent = { Text("${vehicle?.brand} ${vehicle?.model} (${vehicle?.plate?.let { Validators.formatPlate(it) }})") },
                        supportingContent = { Text("Conductor: ${driver?.fullName}\nSalida: ${sdf.format(assignment.departureDate)}") },
                        leadingContent = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }

        if (lastCheckIns.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Últimas Entregas", style = MaterialTheme.typography.titleMedium)
            }
            items(lastCheckIns) { assignment ->
                val vehicle = vehicles.find { it.id == assignment.vehicleId }
                val driver = drivers.find { it.id == assignment.driverId }
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ListItem(
                        headlineContent = { Text("${vehicle?.brand} ${vehicle?.model} (${vehicle?.plate?.let { Validators.formatPlate(it) }})") },
                        supportingContent = { Text("Conductor: ${driver?.fullName}\nEntregado: ${assignment.returnDate?.let { sdf.format(it) }}") },
                        leadingContent = { Icon(Icons.Default.Login, contentDescription = null, tint = Color(0xFF4CAF50)) }
                    )
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

        item {
            Spacer(modifier = Modifier.height(32.dp))
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
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (vehicle.vehiclePhotoUri != null) {
                    AsyncImage(
                        model = vehicle.vehiclePhotoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Placa: ${Validators.formatPlate(vehicle.plate)}")
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
    existingVehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onSave: (Vehicle) -> Unit
) {
    var brand by remember { mutableStateOf(vehicle?.brand ?: "") }
    var model by remember { mutableStateOf(vehicle?.model ?: "") }
    var year by remember { mutableStateOf(vehicle?.year?.toString() ?: "") }
    var plate by remember { mutableStateOf(vehicle?.plate?.let { Validators.formatPlate(it) } ?: "") }
    var color by remember { mutableStateOf(vehicle?.color ?: "") }
    var chassisNumber by remember { mutableStateOf(vehicle?.chassisNumber ?: "") }
    var engineNumber by remember { mutableStateOf(vehicle?.engineNumber ?: "") }
    var insuranceExpiry by remember { mutableStateOf<Date?>(vehicle?.insuranceExpiryDate) }

    // Dropdown de combustible (enum -> String al guardar)
    var fuelType by remember {
        mutableStateOf(vehicle?.fuelType?.let { FuelType.fromLabel(it) })
    }
    var expandedFuel by remember { mutableStateOf(false) }

    var mileage by remember { mutableStateOf(vehicle?.mileage?.toString() ?: "") }
    var description by remember { mutableStateOf(vehicle?.description ?: "") }
    var status by remember { mutableStateOf(vehicle?.status ?: VehicleStatus.AVAILABLE) }
    var expandedStatus by remember { mutableStateOf(false) }

    var insurancePhotoUri by remember { mutableStateOf(vehicle?.insurancePhotoUri) }
    var vehiclePhotoUri by remember { mutableStateOf(vehicle?.vehiclePhotoUri) }
    var registrationPhotoUri by remember { mutableStateOf(vehicle?.registrationPhotoUri) }

    val insurancePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> insurancePhotoUri = uri?.toString() }

    val vehiclePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> vehiclePhotoUri = uri?.toString() }

    val registrationPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> registrationPhotoUri = uri?.toString() }

    var attempted by remember { mutableStateOf(false) }

    val plateRegistry = existingVehicles.map { it.id to it.plate }

    val brandV = Validators.validateRequired(brand, "La marca")
    val modelV = Validators.validateRequired(model, "El modelo")
    val yearV = Validators.validateYear(year)
    val plateV = Validators.validatePlate(plate, plateRegistry, vehicle?.id)
    val fuelV = Validators.validateFuelType(fuelType?.label ?: "")
    val mileageV = Validators.validateMileage(mileage)

    val isValid = listOf(brandV, modelV, yearV, plateV, fuelV, mileageV).all { it.isValid }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle == null) "Agregar Vehículo" else "Editar Vehículo") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("Fotos y Documentación", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhotoPlaceholder(
                                label = "Vehículo",
                                icon = Icons.Default.DirectionsCar,
                                uri = vehiclePhotoUri,
                                onPick = { vehiclePhotoLauncher.launch("image/*") }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhotoPlaceholder(
                                label = "Circulación",
                                icon = Icons.Default.Description,
                                uri = registrationPhotoUri,
                                onPick = { registrationPhotoLauncher.launch("image/*") }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhotoPlaceholder(
                                label = "Seguro",
                                icon = Icons.Default.Shield,
                                uri = insurancePhotoUri,
                                onPick = { insurancePhotoLauncher.launch("image/*") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector de Estado - Solo visible al editar
                    if (vehicle != null) {
                        ExposedDropdownMenuBox(
                            expanded = expandedStatus,
                            onExpandedChange = { expandedStatus = !expandedStatus }
                        ) {
                            OutlinedTextField(
                                value = status.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Estado del Vehículo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                                VehicleStatus.entries.forEach { vs ->
                                    DropdownMenuItem(
                                        text = { Text(vs.label) },
                                        onClick = { status = vs; expandedStatus = false }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = brand, onValueChange = { brand = it },
                        label = { Text("Marca") },
                        isError = attempted && !brandV.isValid,
                        supportingText = if (attempted && !brandV.isValid) {
                            { Text(brandV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = model, onValueChange = { model = it },
                        label = { Text("Modelo") },
                        isError = attempted && !modelV.isValid,
                        supportingText = if (attempted && !modelV.isValid) {
                            { Text(modelV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = year, onValueChange = { year = it.filter { c -> c.isDigit() } },
                            label = { Text("Año") },
                            isError = attempted && !yearV.isValid,
                            supportingText = if (attempted && !yearV.isValid) {
                                { Text(yearV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = color, onValueChange = { color = it },
                            label = { Text("Color") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = plate, onValueChange = { plate = it.uppercase() },
                        label = { Text("Placa") },
                        isError = attempted && !plateV.isValid,
                        supportingText = if (attempted && !plateV.isValid) {
                            { Text(plateV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dropdown de combustible
                    ExposedDropdownMenuBox(
                        expanded = expandedFuel,
                        onExpandedChange = { expandedFuel = !expandedFuel }
                    ) {
                        OutlinedTextField(
                            value = fuelType?.label ?: "Seleccionar combustible",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Combustible") },
                            isError = attempted && !fuelV.isValid,
                            supportingText = if (attempted && !fuelV.isValid) {
                                { Text(fuelV.errorMessage ?: "") }
                            } else null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFuel) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedFuel, onDismissRequest = { expandedFuel = false }) {
                            FuelType.entries.forEach { ft ->
                                DropdownMenuItem(
                                    text = { Text(ft.label) },
                                    onClick = { fuelType = ft; expandedFuel = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mileage, onValueChange = { mileage = it.filter { c -> c.isDigit() } },
                        label = { Text("Kilometraje Actual") },
                        isError = attempted && !mileageV.isValid,
                        supportingText = if (attempted && !mileageV.isValid) {
                            { Text(mileageV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Identificación del Motor/Chasis", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = chassisNumber, onValueChange = { chassisNumber = it.uppercase() },
                        label = { Text("Número de Chasis") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = engineNumber, onValueChange = { engineNumber = it.uppercase() },
                        label = { Text("Número de Motor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Seguro", style = MaterialTheme.typography.labelMedium)
                    DatePickerField(
                        label = "Vencimiento del Seguro",
                        selectedDate = insuranceExpiry,
                        onDateSelected = { insuranceExpiry = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Notas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (isValid) {
                    onSave(
                        Vehicle(
                            id = vehicle?.id ?: UUID.randomUUID().toString(),
                            brand = brand.trim(),
                            model = model.trim(),
                            year = year.toIntOrNull() ?: 0,
                            plate = Validators.normalizePlate(plate),
                            fuelType = fuelType?.label ?: "",
                            mileage = mileage.toLongOrNull() ?: 0L,
                            color = color.trim(),
                            chassisNumber = chassisNumber.trim(),
                            engineNumber = engineNumber.trim(),
                            insuranceExpiryDate = insuranceExpiry,
                            vehiclePhotoUri = vehiclePhotoUri,
                            registrationPhotoUri = registrationPhotoUri,
                            insurancePhotoUri = insurancePhotoUri,
                            status = status,
                            description = description.trim()
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


@Composable
fun VehicleDetailsDialog(
    vehicle: Vehicle,
    maintenanceHistory: List<Maintenance>,
    assignmentHistory: List<Assignment>,
    onDismiss: () -> Unit,
    onStatusChange: (VehicleStatus) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Vehículo") },
        text = {
            LazyColumn {
                item {
                    Text("Marca/Modelo: ${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium)
                    Text("Placa: ${Validators.formatPlate(vehicle.plate)}")
                    Text("Año: ${vehicle.year}")
                    Text("Color: ${vehicle.color.ifBlank { "N/A" }}")
                    Text("Combustible: ${vehicle.fuelType}")
                    Text("Kilometraje: ${vehicle.mileage} km")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Identificación", style = MaterialTheme.typography.titleSmall)
                    Text("Chasis: ${vehicle.chassisNumber.ifBlank { "N/A" }}")
                    Text("Motor: ${vehicle.engineNumber.ifBlank { "N/A" }}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Documentación", style = MaterialTheme.typography.titleSmall)
                    Text("Venc. Seguro: ${vehicle.insuranceExpiryDate?.let { sdf.format(it) } ?: "N/A"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Notas: ${vehicle.description.ifBlank { "Sin observaciones" }}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fotos", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PhotoPlaceholder(
                                label = "Vehículo",
                                icon = Icons.Default.DirectionsCar,
                                uri = vehicle.vehiclePhotoUri
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhotoPlaceholder(
                                label = "Circulación",
                                icon = Icons.Default.Description,
                                uri = vehicle.registrationPhotoUri
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PhotoPlaceholder(
                                label = "Seguro",
                                icon = Icons.Default.Shield,
                                uri = vehicle.insurancePhotoUri
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cambiar Estado:", style = MaterialTheme.typography.titleSmall)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                if (maintenanceHistory.isEmpty()) {
                    item { Text("No hay registros de mantenimiento.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(maintenanceHistory) { maintenance ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("${maintenance.type.label} - ${sdf.format(maintenance.date)}")
                                Text("Estado: ${maintenance.status.label}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Historial de Asignaciones:", style = MaterialTheme.typography.titleSmall)
                }
                if (assignmentHistory.isEmpty()) {
                    item { Text("No hay registros de asignación.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(assignmentHistory) { assignment ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Salida: ${sdf.format(assignment.departureDate)}")
                                assignment.returnDate?.let {
                                    Text("Retorno: ${sdf.format(it)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Estado: ${if (assignment.status == AssignmentStatus.ACTIVE) "Activa" else "Completada"}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
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
    val plateLabel = vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = maintenance.type.label, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Vehículo: ${vehicle?.brand} ${vehicle?.model} ($plateLabel)")
                    Text(text = "Inicio: ${sdf.format(maintenance.date)}")
                    if (maintenance.completionDate != null) {
                        Text(text = "Finalizado: ${sdf.format(maintenance.completionDate)}", style = MaterialTheme.typography.bodySmall)
                    }
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

    var startDate by remember { mutableStateOf<Date?>(maintenance?.date ?: Date()) }
    var completionDate by remember { mutableStateOf<Date?>(maintenance?.completionDate) }
    var nextDate by remember { mutableStateOf<Date?>(maintenance?.nextDate) }

    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val noVehicles = vehicles.isEmpty()
    val selectedVehicle = vehicles.find { it.id == selectedVehicleId }

    val vehicleV: ValidationResult = if (selectedVehicleId.isBlank())
        ValidationResult.invalid("Debe seleccionar un vehículo") else ValidationResult.Valid
    val descV = Validators.validateMaintenanceDescription(description)
    val responsibleV = Validators.validateRequired(responsible, "El responsable")
    val mileageV = if (selectedVehicle != null)
        Validators.validateMaintenanceMileage(currentMileage, selectedVehicle.mileage)
    else
        Validators.validateMileage(currentMileage)
    val nextMileageV = if (selectedVehicle != null)
        Validators.validateNextMileage(nextMileage, selectedVehicle.mileage)
    else
        Validators.validateOptionalMileage(nextMileage)
    val datesV = Validators.validateMaintenanceDates(startDate, completionDate)
    val nextDateV = Validators.validateFutureDate(nextDate, "La próxima fecha")

    val isValid = !noVehicles && listOf(vehicleV, descV, responsibleV, mileageV, nextMileageV, datesV, nextDateV)
        .all { it.isValid }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (maintenance == null) "Registrar Mantenimiento" else "Editar Mantenimiento") },
        text = {
            if (noVehicles) {
                Text("No hay vehículos registrados. Agregá un vehículo primero.")
            } else {
                LazyColumn {
                    item {
                        ExposedDropdownMenuBox(expanded = expandedVehicle, onExpandedChange = { expandedVehicle = !expandedVehicle }) {
                            OutlinedTextField(
                                value = selectedVehicle?.let { "${it.brand} ${it.model} (${Validators.formatPlate(it.plate)})" } ?: "Seleccionar Vehículo",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vehículo") },
                                isError = attempted && !vehicleV.isValid,
                                supportingText = if (attempted && !vehicleV.isValid) {
                                    { Text(vehicleV.errorMessage ?: "") }
                                } else null,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicle) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedVehicle, onDismissRequest = { expandedVehicle = false }) {
                                vehicles.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text("${v.brand} ${v.model} (${Validators.formatPlate(v.plate)})") },
                                        onClick = { selectedVehicleId = v.id; expandedVehicle = false }
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

                        DatePickerField(
                            label = "Fecha de inicio",
                            selectedDate = startDate,
                            onDateSelected = { startDate = it }
                        )

                        DatePickerField(
                            label = "Fecha de finalización (opcional)",
                            selectedDate = completionDate,
                            onDateSelected = { completionDate = it },
                            minDate = startDate,
                            enabled = startDate != null,
                            isError = attempted && !datesV.isValid,
                            supportingText = if (attempted && !datesV.isValid) datesV.errorMessage else null
                        )

                        OutlinedTextField(
                            value = currentMileage,
                            onValueChange = { currentMileage = it.filter { c -> c.isDigit() } },
                            label = { Text("Kilometraje Actual") },
                            isError = attempted && !mileageV.isValid,
                            supportingText = {
                                Text(
                                    if (attempted && !mileageV.isValid) mileageV.errorMessage ?: ""
                                    else selectedVehicle?.let { "Actual del vehículo: ${it.mileage} km" } ?: ""
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = responsible, onValueChange = { responsible = it },
                            label = { Text("Taller/Responsable") },
                            isError = attempted && !responsibleV.isValid,
                            supportingText = if (attempted && !responsibleV.isValid) {
                                { Text(responsibleV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description, onValueChange = { description = it },
                            label = { Text("Descripción") },
                            isError = attempted && !descV.isValid,
                            supportingText = if (attempted && !descV.isValid) {
                                { Text(descV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )

                        DatePickerField(
                            label = "Próxima fecha programada (opcional)",
                            selectedDate = nextDate,
                            onDateSelected = { nextDate = it },
                            minDate = Date(),
                            isError = attempted && !nextDateV.isValid,
                            supportingText = if (attempted && !nextDateV.isValid) nextDateV.errorMessage else null
                        )

                        OutlinedTextField(
                            value = nextMileage,
                            onValueChange = { nextMileage = it.filter { c -> c.isDigit() } },
                            label = { Text("Próximo Kilometraje (opcional)") },
                            isError = attempted && !nextMileageV.isValid,
                            supportingText = if (attempted && !nextMileageV.isValid) {
                                { Text(nextMileageV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    attempted = true
                    if (isValid) {
                        onSave(
                            Maintenance(
                                id = maintenance?.id ?: UUID.randomUUID().toString(),
                                vehicleId = selectedVehicleId,
                                type = type,
                                date = startDate ?: Date(),
                                completionDate = completionDate,
                                currentMileage = currentMileage.toLongOrNull() ?: 0L,
                                description = description.trim(),
                                responsible = responsible.trim(),
                                nextDate = nextDate,
                                nextMileage = nextMileage.toLongOrNull(),
                                status = maintenance?.status ?: MaintenanceStatus.PENDING
                            )
                        )
                    }
                },
                enabled = !noVehicles
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
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
    existingDrivers: List<Driver>,
    onDismiss: () -> Unit,
    onSave: (Driver) -> Unit
) {
    var firstName by remember { mutableStateOf(driver?.firstName ?: "") }
    var lastName by remember { mutableStateOf(driver?.lastName ?: "") }
    var idCardNumber by remember { mutableStateOf(driver?.idCardNumber ?: "") }
    var age by remember { mutableStateOf(driver?.age?.toString() ?: "") }
    var phone by remember { mutableStateOf(driver?.phone ?: "") }
    var licenseNumber by remember { mutableStateOf(driver?.licenseNumber ?: "") }
    var licenseExpiry by remember { mutableStateOf<Date?>(driver?.licenseExpiryDate) }
    val status by remember { mutableStateOf(driver?.status ?: DriverStatus.ACTIVE) }

    var attempted by remember { mutableStateOf(false) }

    val idRegistry = existingDrivers.map { it.id to it.idCardNumber }
    val phoneRegistry = existingDrivers.map { it.id to it.phone }
    val licenseRegistry = existingDrivers.map { it.id to it.licenseNumber }

    val firstNameV = Validators.validateName(firstName, "El nombre")
    val lastNameV = Validators.validateName(lastName, "El apellido")
    val idV = Validators.validateIdCard(idCardNumber, idRegistry, driver?.id)
    val ageV = Validators.validateAge(age)
    val phoneV = Validators.validatePhone(phone, phoneRegistry, driver?.id)
    val licenseNumV = Validators.validateLicenseNumber(licenseNumber, licenseRegistry, driver?.id)
    val expiryV = Validators.validateLicenseExpiry(licenseExpiry)

    val isValid = listOf(firstNameV, lastNameV, idV, ageV, phoneV, licenseNumV, expiryV)
        .all { it.isValid }

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
                    OutlinedTextField(
                        value = firstName, onValueChange = { firstName = it },
                        label = { Text("Nombres") },
                        isError = attempted && !firstNameV.isValid,
                        supportingText = if (attempted && !firstNameV.isValid) {
                            { Text(firstNameV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName, onValueChange = { lastName = it },
                        label = { Text("Apellidos") },
                        isError = attempted && !lastNameV.isValid,
                        supportingText = if (attempted && !lastNameV.isValid) {
                            { Text(lastNameV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = idCardNumber, onValueChange = { idCardNumber = it.uppercase() },
                        label = { Text("Cédula / Identificación") },
                        isError = attempted && !idV.isValid,
                        supportingText = {
                            Text(
                                if (attempted && !idV.isValid) idV.errorMessage ?: ""
                                else "Formato: 13 caracteres alfanuméricos"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                            label = { Text("Edad") },
                            isError = attempted && !ageV.isValid,
                            supportingText = if (attempted && !ageV.isValid) {
                                { Text(ageV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = phone, onValueChange = { phone = it },
                            label = { Text("Teléfono") },
                            isError = attempted && !phoneV.isValid,
                            supportingText = if (attempted && !phoneV.isValid) {
                                { Text(phoneV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier.weight(2f)
                        )
                    }
                    OutlinedTextField(
                        value = licenseNumber, onValueChange = { licenseNumber = it },
                        label = { Text("Número de Licencia") },
                        isError = attempted && !licenseNumV.isValid,
                        supportingText = if (attempted && !licenseNumV.isValid) {
                            { Text(licenseNumV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "Vencimiento de licencia",
                        selectedDate = licenseExpiry,
                        onDateSelected = { licenseExpiry = it },
                        minDate = Date(),
                        isError = attempted && !expiryV.isValid,
                        supportingText = if (attempted && !expiryV.isValid) expiryV.errorMessage else null
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (isValid) {
                    onSave(
                        Driver(
                            id = driver?.id ?: UUID.randomUUID().toString(),
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            idCardNumber = Validators.normalizeIdCard(idCardNumber),
                            age = age.toIntOrNull() ?: 0,
                            phone = phone.trim(),
                            licenseNumber = licenseNumber.trim(),
                            licenseExpiryDate = licenseExpiry ?: Date(),
                            status = status
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun PhotoPlaceholder(label: String, icon: ImageVector, uri: String? = null, onPick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onPick() },
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
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
    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val plateLabel = vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Vehículo: ${vehicle?.brand} ${vehicle?.model} ($plateLabel)", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Conductor: ${driver?.fullName}")
                    Text(text = "Salida: ${sdf.format(assignment.departureDate)}")
                    Text(text = "Retorno planeado: ${sdfDate.format(assignment.plannedReturnDate)}")
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
    val noVehicles = vehicles.isEmpty()
    val noDrivers = drivers.isEmpty()
    val cannotProceed = noVehicles || noDrivers

    var selectedVehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id ?: "") }
    var selectedDriverId by remember { mutableStateOf(drivers.firstOrNull()?.id ?: "") }
    var initialMileage by remember { mutableStateOf(vehicles.find { it.id == selectedVehicleId }?.mileage?.toString() ?: "") }
    var observations by remember { mutableStateOf("") }

    var departureDate by remember { mutableStateOf<Date?>(Date()) }
    var plannedReturnDate by remember {
        mutableStateOf<Date?>(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time)
    }

    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedDriver by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId }
    val mileageV = if (selectedVehicle != null)
        Validators.validateAssignmentInitialMileage(initialMileage, selectedVehicle.mileage)
    else
        Validators.validateMileage(initialMileage)
    val departureV = Validators.validateDepartureDate(departureDate)
    val datesV = Validators.validateAssignmentDates(departureDate, plannedReturnDate)

    val isValid = !cannotProceed && listOf(mileageV, departureV, datesV).all { it.isValid } &&
            selectedVehicleId.isNotBlank() && selectedDriverId.isNotBlank() &&
            initialMileage.isNotBlank() && departureDate != null && plannedReturnDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Asignación") },
        text = {
            if (cannotProceed) {
                Column {
                    if (noVehicles) Text("⚠ No hay vehículos disponibles.", color = MaterialTheme.colorScheme.error)
                    if (noDrivers) Text("⚠ No hay conductores libres y activos.", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Verificá que existan vehículos en estado Disponible y conductores activos sin asignaciones en curso.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn {
                    item {
                        ExposedDropdownMenuBox(expanded = expandedVehicle, onExpandedChange = { expandedVehicle = !expandedVehicle }) {
                            OutlinedTextField(
                                value = vehicles.find { it.id == selectedVehicleId }?.let {
                                    "${Validators.formatPlate(it.plate)} - ${it.model}"
                                } ?: "Seleccionar Vehículo",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vehículo Disponible") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicle) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedVehicle, onDismissRequest = { expandedVehicle = false }) {
                                vehicles.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text("${Validators.formatPlate(v.plate)} - ${v.model}") },
                                        onClick = {
                                            selectedVehicleId = v.id
                                            initialMileage = v.mileage.toString()
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
                                label = { Text("Conductor Libre") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDriver) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedDriver, onDismissRequest = { expandedDriver = false }) {
                                drivers.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.fullName) },
                                        onClick = { selectedDriverId = d.id; expandedDriver = false }
                                    )
                                }
                            }
                        }

                        DatePickerField(
                            label = "Fecha de salida",
                            selectedDate = departureDate,
                            onDateSelected = { departureDate = it },
                            minDate = Date(),
                            maxDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time,
                            isError = attempted && !departureV.isValid,
                            supportingText = if (attempted && !departureV.isValid) departureV.errorMessage else null
                        )

                        DatePickerField(
                            label = "Fecha planeada de retorno",
                            selectedDate = plannedReturnDate,
                            onDateSelected = { plannedReturnDate = it },
                            minDate = departureDate,
                            enabled = departureDate != null,
                            isError = attempted && !datesV.isValid,
                            supportingText = if (attempted && !datesV.isValid) datesV.errorMessage else null
                        )

                        OutlinedTextField(
                            value = initialMileage,
                            onValueChange = { initialMileage = it.filter { c -> c.isDigit() } },
                            label = { Text("Kilometraje Inicial") },
                            isError = attempted && !mileageV.isValid,
                            supportingText = {
                                Text(
                                    if (attempted && !mileageV.isValid) mileageV.errorMessage ?: ""
                                    else selectedVehicle?.let { "Vehículo: ${it.mileage} km" } ?: ""
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = observations, onValueChange = { observations = it },
                            label = { Text("Observaciones de Salida (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    attempted = true
                    if (isValid) {
                        onSave(Assignment(
                            vehicleId = selectedVehicleId,
                            driverId = selectedDriverId,
                            departureDate = departureDate ?: Date(),
                            plannedReturnDate = plannedReturnDate ?: Date(),
                            initialMileage = initialMileage.toLongOrNull() ?: 0L,
                            departureObservations = observations.trim()
                        ))
                    }
                },
                enabled = !cannotProceed
            ) { Text("Asignar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
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
    var attempted by remember { mutableStateOf(false) }

    val mileageV = Validators.validateFinalMileage(finalMileage, assignment.initialMileage)
    val isValid = mileageV.isValid && finalMileage.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Devolución de Vehículo") },
        text = {
            Column {
                Text("Km Inicial: ${assignment.initialMileage}")
                OutlinedTextField(
                    value = finalMileage,
                    onValueChange = { finalMileage = it.filter { c -> c.isDigit() } },
                    label = { Text("Kilometraje Final") },
                    isError = attempted && !mileageV.isValid,
                    supportingText = if (attempted && !mileageV.isValid) {
                        { Text(mileageV.errorMessage ?: "") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observations, onValueChange = { observations = it },
                    label = { Text("Observaciones de Entrega (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                attempted = true
                if (isValid) {
                    onSave(Date(), finalMileage.toLongOrNull() ?: 0L, observations.trim(), nextStatus)
                }
            }) { Text("Confirmar Devolución") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun VehicleStatusDistributionChart(vehicles: List<Vehicle>) {
    val total = vehicles.size.coerceAtLeast(1)
    val distribution = VehicleStatus.entries.map { status ->
        status to vehicles.count { it.status == status }
    }.filter { it.second > 0 }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        distribution.forEach { (status, count) ->
            val fraction = count.toFloat() / total
            val color = when (status) {
                VehicleStatus.AVAILABLE -> Color(0xFF4CAF50)
                VehicleStatus.IN_USE -> Color(0xFF2196F3)
                VehicleStatus.MAINTENANCE -> Color(0xFFFF9800)
                VehicleStatus.OUT_OF_SERVICE -> Color(0xFFF44336)
                VehicleStatus.ASSIGNED -> Color(0xFF9C27B0)
                VehicleStatus.PENDING_REVIEW -> Color(0xFF795548)
                VehicleStatus.INACTIVE -> Color(0xFF9E9E9E)
            }
            Column {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(status.label, style = MaterialTheme.typography.labelSmall)
                    Text("$count (${(fraction * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
    }
}
