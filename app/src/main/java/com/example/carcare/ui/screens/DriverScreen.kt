package com.example.carcare.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carcare.model.*
import com.example.carcare.ui.components.CarCareTopBar
import com.example.carcare.ui.components.ChangeOwnPasswordDialog
import com.example.carcare.ui.components.NotificationItem
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.components.SseRefreshEffect
import com.example.carcare.ui.theme.statusColor
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.AuthViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.NotificacionViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import com.example.carcare.util.ValidationResult
import com.example.carcare.util.Validators
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    driverIdCard: String,
    onBack: () -> Unit,
    vehicleViewModel: VehicleViewModel,
    driverViewModel: DriverViewModel,
    assignmentViewModel: AssignmentViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    authViewModel: AuthViewModel,
    notificacionViewModel: NotificacionViewModel
) {
    val driver = driverViewModel.drivers.find {
        Validators.normalizeIdCard(it.idCardNumber) == Validators.normalizeIdCard(driverIdCard)
    }

    val activeAssignment = driver?.let { d ->
        assignmentViewModel.assignments.find {
            it.driverId == d.id && it.status == AssignmentStatus.ACTIVE
        }
    }

    val assignedVehicle = activeAssignment?.let { a ->
        vehicleViewModel.vehicles.find { it.id == a.vehicleId }
    }

    SseRefreshEffect { entidad ->
        when (entidad) {
            "vehiculos" -> vehicleViewModel.reloadSilently()
            "conductores" -> driverViewModel.reloadSilently()
            "mantenimientos" -> maintenanceViewModel.reloadSilently()
            "asignaciones" -> assignmentViewModel.reloadSilently()
            "notificaciones" -> notificacionViewModel.reload()
        }
    }

    // Carga inicial al entrar al panel: ya hay token (la precarga del arranque —sin
    // sesión— se omite con la API cerrada, así que se carga aquí).
    LaunchedEffect(Unit) {
        driverViewModel.load()
        vehicleViewModel.load()
        assignmentViewModel.load()
        maintenanceViewModel.load()
    }

    // Carga el feed del conductor en cuanto sabemos su id (a partir de su cédula).
    LaunchedEffect(driver?.id) {
        driver?.let { notificacionViewModel.loadForConductor(it.id.toLong()) }
    }

    val isLoading = driverViewModel.isLoading || vehicleViewModel.isLoading ||
            assignmentViewModel.isLoading || maintenanceViewModel.isLoading

    var selectedTab by remember { mutableIntStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CarCareTopBar(
                onAvatarClick = { selectedTab = 1 },
                avatarLetter = driver?.firstName?.trim()?.firstOrNull()?.uppercase() ?: "C",
                subtitle = "Mi panel · Conductor"
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(170)) togetherWith fadeOut(tween(90)) },
                label = "driverTab"
            ) { tab ->
                when (tab) {
                    1 -> if (driver != null) {
                        DriverProfileSection(
                            driver = driver,
                            assignedVehicle = assignedVehicle,
                            onChangePassword = { showChangePassword = true },
                            onLogout = onBack
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Cargando tu perfil…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    else -> DriverHomeSection(
                        driver = driver,
                        driverIdCard = driverIdCard,
                        assignedVehicle = assignedVehicle,
                        activeAssignment = activeAssignment,
                        isLoading = isLoading,
                        driverViewModel = driverViewModel,
                        vehicleViewModel = vehicleViewModel,
                        assignmentViewModel = assignmentViewModel,
                        maintenanceViewModel = maintenanceViewModel,
                        notifications = notificacionViewModel.items,
                        onNotificationClick = { notificacionViewModel.markRead(it.id) },
                        onReport = { showReportDialog = true }
                    )
                }
            }
        }
    }

    if (showReportDialog && assignedVehicle != null && driver != null) {
        ReportMaintenanceDialog(
            vehicle = assignedVehicle,
            driver = driver,
            maintenanceHistory = maintenanceViewModel.getHistoryForVehicle(assignedVehicle.id),
            onDismiss = { showReportDialog = false },
            onSubmit = { maintenance, reportedKm ->
                maintenanceViewModel.addMaintenance(maintenance)
                if (reportedKm > assignedVehicle.mileage) {
                    vehicleViewModel.updateMileage(assignedVehicle.id, reportedKm)
                }
                vehicleViewModel.changeStatus(assignedVehicle.id, VehicleStatus.PENDING_REVIEW)
                showReportDialog = false
            }
        )
    }

    if (showChangePassword && driver != null) {
        ChangeOwnPasswordDialog(
            onDismiss = { showChangePassword = false },
            onConfirm = { actual, nueva ->
                authViewModel.changePassword(
                    cedula = driver.idCardNumber,
                    actual = actual,
                    nueva = nueva,
                    onSuccess = {
                        showChangePassword = false
                        scope.launch { snackbarHostState.showSnackbar("Contraseña actualizada") }
                    },
                    onError = { msg ->
                        showChangePassword = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            }
        )
    }
}

@Composable
private fun DriverHomeSection(
    driver: Driver?,
    driverIdCard: String,
    assignedVehicle: Vehicle?,
    activeAssignment: Assignment?,
    isLoading: Boolean,
    driverViewModel: DriverViewModel,
    vehicleViewModel: VehicleViewModel,
    assignmentViewModel: AssignmentViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    notifications: List<Notificacion>,
    onNotificationClick: (Notificacion) -> Unit,
    onReport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // Cargando todavía y aún no apareció el conductor → estado de carga
            isLoading && driver == null -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando tus datos…", style = MaterialTheme.typography.bodyMedium)
            }

            // Terminó de cargar y no existe ningún conductor con esa cédula
            driver == null && driverViewModel.drivers.isEmpty() -> {
                val loadError = driverViewModel.errorMessage
                    ?: vehicleViewModel.errorMessage
                    ?: assignmentViewModel.errorMessage
                    ?: maintenanceViewModel.errorMessage
                Text(
                    loadError ?: "No pudimos cargar los datos. Revisá tu conexión e intentá de nuevo.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    driverViewModel.loadDrivers()
                    vehicleViewModel.loadVehicles()
                    assignmentViewModel.loadAssignments()
                    maintenanceViewModel.loadMaintenances()
                }) {
                    Text("Reintentar")
                }
            }
            driver == null -> {
                Text(
                    "No encontramos un conductor con la cédula $driverIdCard.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                // Indicador delgado si algo aún está cargando (ej. asignaciones)
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Hola, ${driver.firstName}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text("Tu vehículo asignado", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                if (assignedVehicle != null && activeAssignment != null) {
                    AssignedVehicleCard(
                        vehicle = assignedVehicle,
                        assignment = activeAssignment
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reportar a mantenimiento")
                    }
                } else {
                    Text("No tenés un vehículo asignado actualmente.")
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    "Novedades",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (notifications.isEmpty()) {
                    Text(
                        "Sin novedades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    notifications.take(4).forEach { n ->
                        NotificationItem(notification = n, onClick = { onNotificationClick(n) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverProfileSection(
    driver: Driver,
    assignedVehicle: Vehicle?,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(driver.status.statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, contentDescription = null,
                    tint = driver.status.statusColor, modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(driver.fullName, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(2.dp))
                StatusBadge(status = driver.status)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        ProfileRow("Cédula", driver.idCardNumber)
        ProfileRow("Teléfono", driver.phone)
        ProfileRow("Edad", "${driver.age} años")
        ProfileRow("Licencia vence", sdf.format(driver.licenseExpiryDate))
        ProfileRow(
            "Vehículo asignado",
            assignedVehicle?.let { "${it.brand} ${it.model} (${Validators.formatPlate(it.plate)})" } ?: "Ninguno"
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cambiar contraseña")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AssignedVehicleCard(vehicle: Vehicle, assignment: Assignment) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${vehicle.brand} ${vehicle.model}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Text(text = "Placa: ${Validators.formatPlate(vehicle.plate)}")
            Text(text = "Kilometraje: ${vehicle.mileage} km")
            Spacer(modifier = Modifier.height(8.dp))
            StatusBadge(status = vehicle.status)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Asignación",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(text = "Salida: ${sdf.format(assignment.departureDate)}")
            Text(text = "Retorno planeado: ${sdf.format(assignment.plannedReturnDate)}")
            Text(text = "Km inicial: ${assignment.initialMileage}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportMaintenanceDialog(
    vehicle: Vehicle,
    driver: Driver,
    maintenanceHistory: List<Maintenance>,
    onDismiss: () -> Unit,
    onSubmit: (Maintenance, Long) -> Unit
) {
    val preventiveInterval = 5_000L

    var kmText by remember { mutableStateOf(vehicle.mileage.toString()) }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<MaintenanceType?>(null) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    // Último mantenimiento PREVENTIVO de este vehículo (si lo hay)
    val lastPreventiveKm: Long? = remember(maintenanceHistory) {
        maintenanceHistory
            .filter { it.type == MaintenanceType.PREVENTIVE }
            .maxByOrNull { it.currentMileage }
            ?.currentMileage
    }

    val parsedKm = kmText.toLongOrNull() ?: vehicle.mileage
    val preventiveDue = when {
        lastPreventiveKm == null -> parsedKm >= preventiveInterval
        else -> parsedKm - lastPreventiveKm >= preventiveInterval
    }

    val effectiveType: MaintenanceType? =
        if (preventiveDue) MaintenanceType.PREVENTIVE else selectedType

    val kmV = Validators.validateMaintenanceMileage(kmText, vehicle.mileage)
    val descV = Validators.validateMaintenanceDescription(description)
    val typeV =
        if (effectiveType == null) ValidationResult.invalid("Elegí el tipo de mantenimiento")
        else ValidationResult.Valid
    val isValid = kmV.isValid && descV.isValid && typeV.isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar a mantenimiento") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${vehicle.brand} ${vehicle.model} · ${Validators.formatPlate(vehicle.plate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Kilometraje actual") },
                    isError = attempted && !kmV.isValid,
                    supportingText = if (attempted && !kmV.isValid) {
                        { Text(kmV.errorMessage ?: "") }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Tipo de mantenimiento", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))

                if (preventiveDue) {
                    // Estado A: Preventivo bloqueado por kilometraje
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Preventivo",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    val helper = if (lastPreventiveKm != null) {
                        "+${parsedKm - lastPreventiveKm} km desde el último preventivo " +
                                "($lastPreventiveKm → $parsedKm). Se asigna solo."
                    } else {
                        "Sin preventivo registrado y ya superaste los $preventiveInterval km. " +
                                "Se asigna solo."
                    }
                    Text(
                        helper,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    // Estado B: el conductor elige el tipo
                    ExposedDropdownMenuBox(
                        expanded = typeMenuExpanded,
                        onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType?.let { typeLabel(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Elegí el tipo…") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                            },
                            isError = attempted && !typeV.isValid,
                            supportingText = if (attempted && !typeV.isValid) {
                                { Text(typeV.errorMessage ?: "") }
                            } else null,
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            MaintenanceType.entries
                                .filter { it != MaintenanceType.PREVENTIVE }
                                .forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(typeLabel(type)) },
                                        onClick = {
                                            selectedType = type
                                            typeMenuExpanded = false
                                        }
                                    )
                                }
                        }
                    }
                    val kmToNext = (lastPreventiveKm ?: 0L) + preventiveInterval - parsedKm
                    if (kmToNext > 0) {
                        Text(
                            "Faltan $kmToNext km para el preventivo. Elegí según el problema.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("¿Qué notaste?") },
                    placeholder = { Text("Describí brevemente…") },
                    isError = attempted && !descV.isValid,
                    supportingText = if (attempted && !descV.isValid) {
                        { Text(descV.errorMessage ?: "") }
                    } else null,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (isValid && effectiveType != null) {
                    val km = kmText.toLong()
                    val maintenance = Maintenance(
                        id = UUID.randomUUID().toString(),
                        vehicleId = vehicle.id,
                        type = effectiveType,
                        date = Date(),
                        completionDate = null,
                        currentMileage = km,
                        description = description.trim(),
                        responsible = driver.fullName,
                        nextDate = null,
                        nextMileage = null,
                        status = MaintenanceStatus.IN_PROGRESS
                    )
                    onSubmit(maintenance, km)
                }
            }) { Text("Enviar reporte") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun typeLabel(t: MaintenanceType): String = when (t) {
    MaintenanceType.PREVENTIVE -> "Preventivo"
    MaintenanceType.CORRECTIVE -> "Correctivo"
    MaintenanceType.OIL_CHANGE -> "Cambio de aceite"
    MaintenanceType.BRAKES -> "Frenos"
    MaintenanceType.ENGINE -> "Motor"
    MaintenanceType.TIRES -> "Llantas"
    MaintenanceType.BATTERY -> "Batería"
    MaintenanceType.ALIGNMENT -> "Alineación"
    MaintenanceType.GENERAL_REPAIR -> "Reparación general"
}