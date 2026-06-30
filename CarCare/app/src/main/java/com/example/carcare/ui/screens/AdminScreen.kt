package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carcare.model.*
import com.example.carcare.util.CsvExporter
import com.example.carcare.ui.components.CarCareTopBar
import com.example.carcare.ui.components.DeleteConfirmationDialog
import com.example.carcare.ui.components.SseRefreshEffect
import com.example.carcare.ui.screens.admin.*
import com.example.carcare.ui.viewmodel.*
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    vehicleViewModel: VehicleViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    driverViewModel: DriverViewModel,
    assignmentViewModel: AssignmentViewModel,
    notificacionViewModel: NotificacionViewModel,
    authViewModel: AuthViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ErrorSnackbarEffect(vehicleViewModel.errorMessage, snackbarHostState) { vehicleViewModel.clearError() }
    ErrorSnackbarEffect(driverViewModel.errorMessage, snackbarHostState) { driverViewModel.clearError() }
    ErrorSnackbarEffect(maintenanceViewModel.errorMessage, snackbarHostState) { maintenanceViewModel.clearError() }
    ErrorSnackbarEffect(assignmentViewModel.errorMessage, snackbarHostState) { assignmentViewModel.clearError() }

    SseRefreshEffect { entidad ->
        when (entidad) {
            "vehiculos" -> vehicleViewModel.reloadSilently()
            "conductores" -> driverViewModel.reloadSilently()
            "mantenimientos" -> maintenanceViewModel.reloadSilently()
            "asignaciones" -> assignmentViewModel.reloadSilently()
            "notificaciones" -> notificacionViewModel.reload()
        }
    }

    // Carga inicial al entrar al panel: ya hay token (la API está cerrada, así que la
    // precarga del arranque —sin sesión— se omite y se hace aquí).
    LaunchedEffect(Unit) {
        vehicleViewModel.load()
        driverViewModel.load()
        maintenanceViewModel.load()
        assignmentViewModel.load()
        notificacionViewModel.loadForAdmin()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }
    val tabs = listOf("Panel", "Vehículos", "Taller", "Conduct.", "Asign.")
    val context = LocalContext.current

    var showVehicleForm by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToShowDetails by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }

    var showMaintenanceForm by remember { mutableStateOf(false) }
    var showGeneralHistory by remember { mutableStateOf(false) }
    var maintenanceToEdit by remember { mutableStateOf<Maintenance?>(null) }
    var maintenanceToShowDetails by remember { mutableStateOf<Maintenance?>(null) }
    var maintenanceToDelete by remember { mutableStateOf<Maintenance?>(null) }

    var showDriverForm by remember { mutableStateOf(false) }
    var driverToEdit by remember { mutableStateOf<Driver?>(null) }
    var driverToShowDetails by remember { mutableStateOf<Driver?>(null) }
    var driverToDelete by remember { mutableStateOf<Driver?>(null) }
    var newDriverCredentials by remember { mutableStateOf<Driver?>(null) }
    var driverToChangePassword by remember { mutableStateOf<Driver?>(null) }

    var showAssignmentForm by remember { mutableStateOf(false) }
    var showAssignmentHistory by remember { mutableStateOf(false) }
    var assignmentToEdit by remember { mutableStateOf<com.example.carcare.model.Assignment?>(null) }
    var assignmentToDelete by remember { mutableStateOf<com.example.carcare.model.Assignment?>(null) }
    var assignmentToShowDetails by remember { mutableStateOf<com.example.carcare.model.Assignment?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CarCareTopBar(onAvatarClick = { showProfile = true }) },
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
                                else -> Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
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
                2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SmallFloatingActionButton(onClick = {
                        CsvExporter.exportMaintenances(
                            context, maintenanceViewModel.maintenances, vehicleViewModel.vehicles
                        )
                    }) { Icon(Icons.Default.FileDownload, contentDescription = "Exportar CSV") }
                    Spacer(Modifier.height(12.dp))
                    FloatingActionButton(onClick = {
                        maintenanceToEdit = null
                        showMaintenanceForm = true
                    }) { Icon(Icons.Default.PostAdd, contentDescription = "Registrar Mantenimiento") }
                }
                3 -> FloatingActionButton(onClick = {
                    driverToEdit = null
                    showDriverForm = true
                }) { Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Conductor") }
                4 -> FloatingActionButton(onClick = {
                    assignmentToEdit = null
                    showAssignmentForm = true
                }) { Icon(Icons.Default.Add, contentDescription = "Nueva Asignación") }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {


            val sectionLoading = when (selectedTab) {
                0 -> vehicleViewModel.isLoading || driverViewModel.isLoading ||
                        maintenanceViewModel.isLoading || assignmentViewModel.isLoading
                1 -> vehicleViewModel.isLoading
                2 -> maintenanceViewModel.isLoading
                3 -> driverViewModel.isLoading
                4 -> assignmentViewModel.isLoading
                else -> false
            }
            if (sectionLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cambio de pestaña con crossfade corto: continuidad sin lentitud.
            // Acción frecuente → animación drástica no; 170ms in / 90ms out (exit más rápido).
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { fadeIn(tween(170)) togetherWith fadeOut(tween(90)) },
                label = "adminTabContent"
            ) { tab ->
                Column(Modifier.fillMaxSize()) {
                    when (tab) {
                        0 -> DashboardSection(
                            vehicles = vehicleViewModel.vehicles,
                            drivers = driverViewModel.drivers,
                            maintenances = maintenanceViewModel.maintenances,
                            assignments = assignmentViewModel.assignments,
                            notifications = notificacionViewModel.items,
                            onMarkAllRead = { notificacionViewModel.markAllRead() },
                            onNotificationClick = { n ->
                                // Deep-link: abrir el detalle de la entidad referida por la notificación.
                                when (n.type) {
                                    NotificationType.VEHICULO ->
                                        vehicleViewModel.vehicles.firstOrNull { it.id == n.entityId }?.let { vehicleToShowDetails = it }
                                    NotificationType.CONDUCTOR, NotificationType.SEGURIDAD ->
                                        driverViewModel.drivers.firstOrNull { it.id == n.entityId }?.let { driverToShowDetails = it }
                                    NotificationType.MANTENIMIENTO ->
                                        maintenanceViewModel.maintenances.firstOrNull { it.id == n.entityId }?.let { maintenanceToShowDetails = it }
                                    NotificationType.ASIGNACION ->
                                        assignmentViewModel.assignments.firstOrNull { it.id == n.entityId }?.let { assignmentToShowDetails = it }
                                    NotificationType.OTRO -> {}
                                }
                                notificacionViewModel.markRead(n.id)
                            }
                        )
                        1 -> {
                            SearchBar(
                                query = vehicleViewModel.searchQuery,
                                onQueryChange = { vehicleViewModel.onSearchQueryChange(it) },
                                label = "Buscar vehículo (marca, placa...)"
                            )
                            VehicleListSection(
                                vehicles = vehicleViewModel.filteredVehicles,
                                onVehicleClick = { vehicleToShowDetails = it },
                                onDelete = { vehicleToDelete = it }
                            )
                        }
                        2 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SearchBar(
                                        query = maintenanceViewModel.searchQuery,
                                        onQueryChange = { maintenanceViewModel.onSearchQueryChange(it) },
                                        label = "Buscar mantenimiento..."
                                    )
                                }
                                IconButton(onClick = { showGeneralHistory = true }) {
                                    Icon(Icons.Default.History, contentDescription = "Historial General")
                                }
                            }
                            val pendingMaintenances = remember(
                                maintenanceViewModel.maintenances,
                                maintenanceViewModel.searchQuery,
                                vehicleViewModel.vehicles
                            ) {
                                maintenanceViewModel.getFilteredMaintenances(vehicleViewModel.vehicles)
                                    .filter { it.status != MaintenanceStatus.COMPLETED }
                            }
                            MaintenanceListSection(
                                maintenances = pendingMaintenances,
                                vehicles = vehicleViewModel.vehicles,
                                onClick = { maintenanceToShowDetails = it },
                                onDelete = { maintenanceToDelete = it },
                                onStatusChange = { m, s ->
                                    maintenanceViewModel.updateStatus(m.id, s)
                                    if (s == MaintenanceStatus.COMPLETED && m.vehicleId.isNotBlank()) {
                                        vehicleViewModel.changeStatus(m.vehicleId, VehicleStatus.AVAILABLE)
                                    }
                                }
                            )
                        }
                        3 -> {
                            SearchBar(
                                query = driverViewModel.searchQuery,
                                onQueryChange = { driverViewModel.onSearchQueryChange(it) },
                                label = "Buscar conductor..."
                            )
                            DriverListSection(
                                drivers = driverViewModel.filteredDrivers,
                                onDriverClick = { driverToShowDetails = it },
                                onDelete = { driverToDelete = it }
                            )
                        }
                        4 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SearchBar(
                                        query = assignmentViewModel.searchQuery,
                                        onQueryChange = { assignmentViewModel.onSearchQueryChange(it) },
                                        label = "Buscar asignación..."
                                    )
                                }
                                IconButton(onClick = { showAssignmentHistory = true }) {
                                    Icon(Icons.Default.History, contentDescription = "Historial de Asignaciones")
                                }
                            }
                            val activeAssignments = remember(
                                assignmentViewModel.assignments,
                                assignmentViewModel.searchQuery,
                                vehicleViewModel.vehicles,
                                driverViewModel.drivers
                            ) {
                                assignmentViewModel.getFilteredAssignments(vehicleViewModel.vehicles, driverViewModel.drivers)
                                    .filter { it.status == AssignmentStatus.ACTIVE || it.status == AssignmentStatus.PENDING_ACCEPTANCE }
                            }
                            AssignmentListSection(
                                assignments = activeAssignments,
                                vehicles = vehicleViewModel.vehicles,
                                drivers = driverViewModel.drivers,
                                onClick = { assignmentToShowDetails = it },
                                onDelete = { assignmentToDelete = it }
                            )
                        }
                    }
                }
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
                if (maintenanceToEdit == null) {
                    maintenanceViewModel.addMaintenance(maintenance)
                    vehicleViewModel.changeStatus(maintenance.vehicleId, VehicleStatus.MAINTENANCE)
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
            existingDrivers = driverViewModel.drivers,
            onDismiss = { showDriverForm = false },
            onSave = { driver ->
                if (driverToEdit == null) {
                    // Al crear, el backend genera la cuenta de acceso → mostramos sus credenciales.
                    driverViewModel.addDriver(driver) { newDriverCredentials = driver }
                } else {
                    driverViewModel.updateDriver(driver)
                }
                showDriverForm = false
            }
        )
    }

    if (showAssignmentForm) {
        // Memoizado: solo se recalcula cuando cambian las listas o la asignación en edición.
        val busyDriverIds = remember(assignmentViewModel.assignments, assignmentToEdit) {
            assignmentViewModel.assignments
                // Un conductor está ocupado tanto si tiene una carrera ACTIVA como si tiene una
                // PENDIENTE de aceptar: el backend rechaza una segunda asignación en ambos casos.
                .filter {
                    (it.status == AssignmentStatus.ACTIVE || it.status == AssignmentStatus.PENDING_ACCEPTANCE) &&
                            it.id != assignmentToEdit?.id
                }
                .map { it.driverId }
                .toSet()
        }
        val selectableVehicles = remember(vehicleViewModel.vehicles, assignmentToEdit) {
            vehicleViewModel.vehicles.filter {
                (it.status == VehicleStatus.AVAILABLE && !it.isInsuranceExpired) || it.id == assignmentToEdit?.vehicleId
            }
        }
        val selectableDrivers = remember(driverViewModel.drivers, busyDriverIds, assignmentToEdit) {
            driverViewModel.drivers.filter {
                // Un conductor cuya cédula es de una cuenta ADMIN o con licencia VENCIDA no es asignable.
                (it.status == DriverStatus.ACTIVE && !it.isAdmin && !it.isLicenseExpired && it.id !in busyDriverIds) ||
                        it.id == assignmentToEdit?.driverId
            }
        }

        AssignmentFormDialog(
            assignment = assignmentToEdit,
            vehicles = selectableVehicles,
            drivers = selectableDrivers,
            onDismiss = {
                showAssignmentForm = false
                assignmentToEdit = null
            },
            onSave = { assignmentData ->
                val original = assignmentToEdit
                when {
                    original == null ->
                        assignmentViewModel.addAssignment(assignmentData) { vehicleViewModel.loadVehicles() }
                    assignmentData.status == AssignmentStatus.COMPLETED && original.status != AssignmentStatus.COMPLETED ->
                        // Completar pasa por el endpoint transaccional: registra retorno,
                        // actualiza km del vehículo y lo libera, todo en una operación.
                        assignmentViewModel.completeAssignment(
                            assignmentId = original.id,
                            returnDate = assignmentData.returnDate ?: Date(),
                            finalMileage = assignmentData.finalMileage ?: assignmentData.initialMileage,
                            observations = assignmentData.returnObservations,
                            nextStatus = VehicleStatus.AVAILABLE
                        ) { vehicleViewModel.loadVehicles() }
                    else ->
                        assignmentViewModel.updateAssignment(assignmentData) { vehicleViewModel.loadVehicles() }
                }
                showAssignmentForm = false
                assignmentToEdit = null
            }
        )
    }

    if (vehicleToShowDetails != null) {
        val vehicleAssignments = remember(assignmentViewModel.assignments, vehicleToShowDetails) {
            assignmentViewModel.assignments.filter { it.vehicleId == vehicleToShowDetails!!.id }
        }
        VehicleDetailsDialog(
            vehicle = vehicleToShowDetails!!,
            maintenanceHistory = maintenanceViewModel.getHistoryForVehicle(vehicleToShowDetails!!.id),
            assignmentHistory = vehicleAssignments,
            onDismiss = { vehicleToShowDetails = null },
            onStatusChange = { newStatus ->
                vehicleViewModel.changeStatus(vehicleToShowDetails!!.id, newStatus)
                vehicleToShowDetails = vehicleToShowDetails!!.copy(status = newStatus)
            },
            onAssignmentClick = { assignment ->
                assignmentToShowDetails = assignment
            },
            onEdit = {
                vehicleToEdit = vehicleToShowDetails
                vehicleToShowDetails = null
                showVehicleForm = true
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
            },
            onChangePassword = { driverToChangePassword = driverToShowDetails },
            onEdit = {
                driverToEdit = driverToShowDetails
                driverToShowDetails = null
                showDriverForm = true
            }
        )
    }

    newDriverCredentials?.let { created ->
        DriverCredentialsDialog(
            driver = created,
            onDismiss = { newDriverCredentials = null }
        )
    }

    driverToChangePassword?.let { target ->
        ChangePasswordDialog(
            driver = target,
            onDismiss = { driverToChangePassword = null },
            onConfirm = { newPassword ->
                driverViewModel.resetPassword(
                    driverId = target.id,
                    newPassword = newPassword,
                    onSuccess = {
                        driverToChangePassword = null
                        scope.launch { snackbarHostState.showSnackbar("Contraseña actualizada") }
                    },
                    onError = { msg ->
                        driverToChangePassword = null
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            }
        )
    }

    if (vehicleToDelete != null) {
        val hasActiveAssignment = assignmentViewModel.assignments.any {
            it.vehicleId == vehicleToDelete!!.id && it.status == AssignmentStatus.ACTIVE
        }
        DeleteConfirmationDialog(
            title = "Eliminar Vehículo",
            message = if (hasActiveAssignment)
                "Este vehículo tiene una asignación ACTIVA. Completala o eliminala antes de borrar el vehículo."
            else "¿Eliminar el vehículo ${vehicleToDelete?.brand} ${vehicleToDelete?.plate}? " +
                    "Su historial de mantenimientos y asignaciones se conservará.",
            onConfirm = {
                if (!hasActiveAssignment) {
                    vehicleViewModel.deleteVehicle(vehicleToDelete!!.id)
                    scope.launch { snackbarHostState.showSnackbar("Vehículo eliminado") }
                }
            },
            onDismiss = { vehicleToDelete = null }
        )
    }

    if (maintenanceToShowDetails != null) {
        MaintenanceDetailsDialog(
            maintenance = maintenanceToShowDetails!!,
            vehicle = vehicleViewModel.vehicles.find { it.id == maintenanceToShowDetails!!.vehicleId },
            onDismiss = { maintenanceToShowDetails = null },
            onEdit = {
                maintenanceToEdit = maintenanceToShowDetails
                maintenanceToShowDetails = null
                showMaintenanceForm = true
            }
        )
    }

    if (assignmentToShowDetails != null) {
        AssignmentDetailsDialog(
            assignment = assignmentToShowDetails!!,
            vehicle = vehicleViewModel.vehicles.find { it.id == assignmentToShowDetails!!.vehicleId },
            driver = driverViewModel.drivers.find { it.id == assignmentToShowDetails!!.driverId },
            onFetchPhoto = { id: String, initial: Boolean, cb: (String?) -> Unit ->
                assignmentViewModel.fetchPhoto(id, initial, cb)
            },
            onDismiss = { assignmentToShowDetails = null },
            onEdit = {
                assignmentToEdit = assignmentToShowDetails
                assignmentToShowDetails = null
                showAssignmentForm = true
            }
        )
    }

    if (showGeneralHistory) {
        GeneralMaintenanceHistoryDialog(
            maintenances = maintenanceViewModel.maintenances.filter { it.status == MaintenanceStatus.COMPLETED },
            vehicles = vehicleViewModel.vehicles,
            onDismiss = { showGeneralHistory = false },
            onMaintenanceClick = {
                maintenanceToShowDetails = it
            }
        )
    }

    if (showAssignmentHistory) {
        GeneralAssignmentHistoryDialog(
            assignments = assignmentViewModel.assignments.filter {
                it.status == AssignmentStatus.COMPLETED || it.status == AssignmentStatus.REJECTED
            },
            vehicles = vehicleViewModel.vehicles,
            drivers = driverViewModel.drivers,
            onDismiss = { showAssignmentHistory = false },
            onAssignmentClick = { assignment ->
                assignmentToShowDetails = assignment
            }
        )
    }

    if (maintenanceToDelete != null) {
        DeleteConfirmationDialog(
            title = "Eliminar Mantenimiento",
            message = "¿Estás seguro de que deseas eliminar este registro de mantenimiento?",
            onConfirm = {
                maintenanceViewModel.deleteMaintenance(maintenanceToDelete!!.id)
                scope.launch { snackbarHostState.showSnackbar("Mantenimiento eliminado") }
            },
            onDismiss = { maintenanceToDelete = null }
        )
    }

    if (driverToDelete != null) {
        val hasAssignments = assignmentViewModel.assignments.any { it.driverId == driverToDelete!!.id }
        DeleteConfirmationDialog(
            title = "Eliminar Conductor",
            message = if (hasAssignments) "Este conductor tiene asignaciones registradas y no puede ser eliminado."
            else "¿Estás seguro de que deseas eliminar a ${driverToDelete?.fullName}?",
            onConfirm = {
                if (!hasAssignments) {
                    driverViewModel.deleteDriver(driverToDelete!!.id)
                    scope.launch { snackbarHostState.showSnackbar("Conductor eliminado") }
                }
            },
            onDismiss = { driverToDelete = null }
        )
    }

    if (assignmentToDelete != null) {
        DeleteConfirmationDialog(
            title = "Eliminar Asignación",
            message = "¿Estás seguro de que deseas eliminar esta asignación?",
            onConfirm = {
                assignmentViewModel.deleteAssignment(assignmentToDelete!!.id) { vehicleViewModel.loadVehicles() }
                scope.launch { snackbarHostState.showSnackbar("Asignación eliminada") }
            },
            onDismiss = { assignmentToDelete = null }
        )
    }

    // Perfil del admin: se abre desde el avatar. Overlay full-screen sobre el panel.
    if (showProfile) {
        AdminProfileScreen(
            onBack = { showProfile = false },
            onLogout = onBack,
            onChangePassword = { actual, nueva, onSuccess, onError ->
                authViewModel.changePassword(
                    cedula = com.example.carcare.data.AuthSession.cedula.orEmpty(),
                    actual = actual,
                    nueva = nueva,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        )
    }
}

/**
 * Muestra un error en el snackbar cuando aparece y luego lo limpia.
 * Reutilizable para cualquier ViewModel con un errorMessage nullable.
 */
@Composable
private fun ErrorSnackbarEffect(
    message: String?,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit
) {
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onShown()
        }
    }
}