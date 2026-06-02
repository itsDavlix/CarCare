package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carcare.model.*
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.components.SseRefreshEffect
import com.example.carcare.ui.viewmodel.AssignmentViewModel
import com.example.carcare.ui.viewmodel.DriverViewModel
import com.example.carcare.ui.viewmodel.MaintenanceViewModel
import com.example.carcare.ui.viewmodel.VehicleViewModel
import com.example.carcare.util.ValidationResult
import com.example.carcare.util.Validators
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
    maintenanceViewModel: MaintenanceViewModel
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
        }
    }

    val isLoading = driverViewModel.isLoading || vehicleViewModel.isLoading ||
            assignmentViewModel.isLoading || maintenanceViewModel.isLoading

    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Conductor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
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
                            onClick = { showReportDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reportar a mantenimiento")
                        }
                    } else {
                        Text("No tenés un vehículo asignado actualmente.")
                    }
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