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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import com.example.carcare.ui.components.ProfileSettingsSection
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

    // Asignación que el admin creó y el conductor todavía no aceptó (check-out pendiente).
    val pendingAssignment = driver?.let { d ->
        assignmentViewModel.assignments.find {
            it.driverId == d.id && it.status == AssignmentStatus.PENDING_ACCEPTANCE
        }
    }

    val assignedVehicle = activeAssignment?.let { a ->
        vehicleViewModel.vehicles.find { it.id == a.vehicleId }
    }

    val pendingVehicle = pendingAssignment?.let { a ->
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
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun toast(msg: String) = scope.launch { snackbarHostState.showSnackbar(msg) }

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
                        pendingAssignment = pendingAssignment,
                        pendingVehicle = pendingVehicle,
                        isLoading = isLoading,
                        driverViewModel = driverViewModel,
                        vehicleViewModel = vehicleViewModel,
                        assignmentViewModel = assignmentViewModel,
                        maintenanceViewModel = maintenanceViewModel,
                        notifications = notificacionViewModel.items,
                        onNotificationClick = { notificacionViewModel.markRead(it.id) },
                        onReport = { showReportDialog = true },
                        onAccept = { showAcceptDialog = true },
                        onReject = { showRejectDialog = true },
                        onReturn = { showReturnDialog = true }
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
            onSubmit = { maintenance, _ ->
                // Una sola operación atómica en el backend: crea el mantenimiento, deja el
                // vehículo EN REVISIÓN y actualiza su km (todo a partir del mantenimiento).
                // Antes eran 3 llamadas sueltas y la de km daba 403 al conductor (Fase 2).
                maintenanceViewModel.reportar(maintenance)
                showReportDialog = false
            }
        )
    }

    if (showAcceptDialog && pendingAssignment != null && pendingVehicle != null) {
        AcceptAssignmentDialog(
            vehicle = pendingVehicle,
            assignment = pendingAssignment,
            onDismiss = { showAcceptDialog = false },
            onConfirm = { km, fuel, conditionOk, obs ->
                assignmentViewModel.acceptAssignment(
                    assignmentId = pendingAssignment.id,
                    initialMileage = km,
                    fuelLevel = fuel,
                    conditionOk = conditionOk,
                    observations = obs,
                    onSuccess = {
                        vehicleViewModel.reloadSilently()
                        toast("Asignación aceptada. ¡Buen viaje!")
                    }
                )
                showAcceptDialog = false
            }
        )
    }

    if (showRejectDialog && pendingAssignment != null && pendingVehicle != null) {
        RejectAssignmentDialog(
            vehicle = pendingVehicle,
            onDismiss = { showRejectDialog = false },
            onConfirm = { motivo ->
                assignmentViewModel.rejectAssignment(
                    assignmentId = pendingAssignment.id,
                    reason = motivo,
                    onSuccess = {
                        vehicleViewModel.reloadSilently()
                        toast("Asignación rechazada.")
                    }
                )
                showRejectDialog = false
            }
        )
    }

    if (showReturnDialog && activeAssignment != null && assignedVehicle != null) {
        ReturnVehicleDialog(
            vehicle = assignedVehicle,
            assignment = activeAssignment,
            onDismiss = { showReturnDialog = false },
            onConfirm = { km, fuel, conditionOk, obs ->
                assignmentViewModel.deliverAssignment(
                    assignmentId = activeAssignment.id,
                    returnDate = Date(),
                    finalMileage = km,
                    observations = obs,
                    fuelLevel = fuel,
                    conditionOk = conditionOk,
                    onSuccess = {
                        vehicleViewModel.reloadSilently()
                        toast("Vehículo entregado. ¡Gracias!")
                    }
                )
                showReturnDialog = false
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
    pendingAssignment: Assignment?,
    pendingVehicle: Vehicle?,
    isLoading: Boolean,
    driverViewModel: DriverViewModel,
    vehicleViewModel: VehicleViewModel,
    assignmentViewModel: AssignmentViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    notifications: List<Notificacion>,
    onNotificationClick: (Notificacion) -> Unit,
    onReport: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onReturn: () -> Unit
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

                when {
                    // 1) Hay una asignación esperando que la acepte (check-out)
                    pendingAssignment != null && pendingVehicle != null -> {
                        Text("Asignación por aceptar", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        PendingAssignmentCard(vehicle = pendingVehicle, assignment = pendingAssignment)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aceptar asignación")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onReject, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rechazar")
                        }
                    }

                    // 2) Carrera en curso (ya aceptada)
                    assignedVehicle != null && activeAssignment != null -> {
                        Text("Tu carrera activa", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        AssignedVehicleCard(vehicle = assignedVehicle, assignment = activeAssignment)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onReturn, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entregar vehículo")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reportar a mantenimiento")
                        }
                    }

                    // 3) Sin nada asignado
                    else -> {
                        Text("Tu vehículo asignado", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tenés un vehículo asignado actualmente.")
                    }
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

        Spacer(modifier = Modifier.height(20.dp))
        ProfileSettingsSection()

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
            if (assignment.overdue) {
                OverdueBanner(days = assignment.daysOverdue)
                Spacer(modifier = Modifier.height(12.dp))
            }
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
                text = "Carrera",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(text = "Salida: ${sdf.format(assignment.departureDate)}")
            Text(
                text = "Retorno previsto: ${sdf.format(assignment.plannedReturnDate)}",
                color = if (assignment.overdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
            Text(text = "Km inicial: ${assignment.initialMileage}")
            assignment.fuelLevelInitial?.let {
                Text(text = "Combustible al salir: ${it.label}")
            }
        }
    }
}

/** Aviso crítico: la fecha prevista de retorno ya pasó. Visible de entrada, sin animación que lo oculte. */
@Composable
private fun OverdueBanner(days: Long) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "Devolución vencida",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (days <= 1L) "Te pasaste 1 día. Entregá el vehículo cuanto antes."
                    else "Te pasaste $days días. Entregá el vehículo cuanto antes.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PendingAssignmentCard(vehicle: Vehicle, assignment: Assignment) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${vehicle.brand} ${vehicle.model}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = assignment.status)
            }
            Text(text = "Placa: ${Validators.formatPlate(vehicle.plate)}")
            Text(text = "Kilometraje actual: ${vehicle.mileage} km")

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(text = "Salida: ${sdf.format(assignment.departureDate)}")
            Text(text = "Retorno previsto: ${sdf.format(assignment.plannedReturnDate)}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Revisá el vehículo y registrá kilometraje y combustible al aceptar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelLevelDropdown(
    selected: FuelLevel?,
    onSelect: (FuelLevel) -> Unit,
    isError: Boolean,
    errorText: String?
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Nivel de combustible") },
            placeholder = { Text("Elegí el nivel…") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            supportingText = if (isError && errorText != null) { { Text(errorText) } } else null,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FuelLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.label) },
                    onClick = { onSelect(level); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ConditionRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("¿Está en condiciones óptimas?", style = MaterialTheme.typography.bodyLarge)
            Text(
                if (checked) "Sí, todo en orden" else "No, requiere atención",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun AcceptAssignmentDialog(
    vehicle: Vehicle,
    assignment: Assignment,
    onDismiss: () -> Unit,
    onConfirm: (km: Long, fuel: FuelLevel, conditionOk: Boolean, obs: String) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var kmText by remember { mutableStateOf(vehicle.mileage.toString()) }
    var fuel by remember { mutableStateOf<FuelLevel?>(null) }
    var conditionOk by remember { mutableStateOf(true) }
    var obs by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }

    val kmV = Validators.validateAssignmentInitialMileage(kmText, vehicle.mileage)
    val fuelV = if (fuel == null) ValidationResult.invalid("Elegí el nivel de combustible") else ValidationResult.Valid
    val obsV = if (!conditionOk && obs.isBlank())
        ValidationResult.invalid("Contá qué problema tiene el vehículo") else ValidationResult.Valid
    val isValid = kmV.isValid && fuelV.isValid && obsV.isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aceptar asignación") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    "${vehicle.brand} ${vehicle.model} · ${Validators.formatPlate(vehicle.plate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Retorno previsto: ${sdf.format(assignment.plannedReturnDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Kilometraje actual") },
                    isError = attempted && !kmV.isValid,
                    supportingText = if (attempted && !kmV.isValid) { { Text(kmV.errorMessage ?: "") } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                FuelLevelDropdown(
                    selected = fuel,
                    onSelect = { fuel = it },
                    isError = attempted && !fuelV.isValid,
                    errorText = fuelV.errorMessage
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConditionRow(checked = conditionOk, onChange = { conditionOk = it })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = obs,
                    onValueChange = { obs = it },
                    label = { Text(if (conditionOk) "Observaciones (opcional)" else "¿Qué problema tiene? (obligatorio)") },
                    isError = attempted && !obsV.isValid,
                    supportingText = if (attempted && !obsV.isValid) { { Text(obsV.errorMessage ?: "") } } else null,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (isValid && fuel != null) onConfirm(kmText.toLong(), fuel!!, conditionOk, obs.trim())
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun RejectAssignmentDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onConfirm: (motivo: String) -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    val motivoV = Validators.validateRequired(motivo, "El motivo")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rechazar asignación") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${vehicle.brand} ${vehicle.model} · ${Validators.formatPlate(vehicle.plate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo del rechazo") },
                    placeholder = { Text("Ej: el vehículo no está en el punto de salida") },
                    isError = attempted && !motivoV.isValid,
                    supportingText = if (attempted && !motivoV.isValid) { { Text(motivoV.errorMessage ?: "") } } else null,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (motivoV.isValid) onConfirm(motivo.trim())
            }) { Text("Rechazar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ReturnVehicleDialog(
    vehicle: Vehicle,
    assignment: Assignment,
    onDismiss: () -> Unit,
    onConfirm: (km: Long, fuel: FuelLevel, conditionOk: Boolean, obs: String) -> Unit
) {
    var kmText by remember { mutableStateOf(vehicle.mileage.toString()) }
    var fuel by remember { mutableStateOf<FuelLevel?>(null) }
    var conditionOk by remember { mutableStateOf(true) }
    var obs by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }

    // El km de entrega se vuelve el más reciente del vehículo: nunca menor al inicial ni al actual.
    val baseline = maxOf(assignment.initialMileage, vehicle.mileage)
    val kmV = Validators.validateFinalMileage(kmText, baseline)
    val fuelV = if (fuel == null) ValidationResult.invalid("Elegí el nivel de combustible") else ValidationResult.Valid
    val obsV = if (!conditionOk && obs.isBlank())
        ValidationResult.invalid("Contá qué problema tiene el vehículo") else ValidationResult.Valid
    val isValid = kmV.isValid && fuelV.isValid && obsV.isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entregar vehículo") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    "${vehicle.brand} ${vehicle.model} · ${Validators.formatPlate(vehicle.plate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Km inicial de la carrera: ${assignment.initialMileage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Kilometraje de entrega") },
                    isError = attempted && !kmV.isValid,
                    supportingText = if (attempted && !kmV.isValid) { { Text(kmV.errorMessage ?: "") } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                FuelLevelDropdown(
                    selected = fuel,
                    onSelect = { fuel = it },
                    isError = attempted && !fuelV.isValid,
                    errorText = fuelV.errorMessage
                )
                Spacer(modifier = Modifier.height(12.dp))
                ConditionRow(checked = conditionOk, onChange = { conditionOk = it })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = obs,
                    onValueChange = { obs = it },
                    label = { Text(if (conditionOk) "Observaciones (opcional)" else "¿Qué problema tiene? (obligatorio)") },
                    isError = attempted && !obsV.isValid,
                    supportingText = if (attempted && !obsV.isValid) { { Text(obsV.errorMessage ?: "") } } else null,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (isValid && fuel != null) onConfirm(kmText.toLong(), fuel!!, conditionOk, obs.trim())
            }) { Text("Entregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
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