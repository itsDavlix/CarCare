package com.example.carcare.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import com.example.carcare.model.FuelType
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceType
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus
import com.example.carcare.model.Assignment as AssignmentModel
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.ui.components.Base64Image
import com.example.carcare.ui.components.DatePickerField
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.util.ValidationResult
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*
import com.example.carcare.ui.theme.statusColor

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
    var insurancePolicy by remember { mutableStateOf(vehicle?.insurancePolicy ?: "") }
    var insuranceExpiry by remember { mutableStateOf(vehicle?.insuranceExpiryDate) }

    var fuelType by remember { mutableStateOf(vehicle?.fuelType ?: FuelType.GASOLINE) }
    var expandedFuel by remember { mutableStateOf(false) }

    var mileage by remember { mutableStateOf(vehicle?.mileage?.toString() ?: "") }
    var description by remember { mutableStateOf(vehicle?.description ?: "") }

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
    val chassisRegistry = existingVehicles.map { it.id to it.chassisNumber }
    val engineRegistry = existingVehicles.map { it.id to it.engineNumber }

    val brandV = Validators.validateRequired(brand, "La marca")
    val modelV = Validators.validateRequired(model, "El modelo")
    val yearV = Validators.validateYear(year)
    val plateV = Validators.validatePlate(plate, plateRegistry, vehicle?.id)
    val fuelV = Validators.validateFuelType(fuelType.label)
    val mileageV = Validators.validateMileage(mileage)
    val chassisV = Validators.validateUniqueOptionalField(chassisNumber, "El número de chasis", chassisRegistry, vehicle?.id)
    val engineV = Validators.validateUniqueOptionalField(engineNumber, "El número de motor", engineRegistry, vehicle?.id)
    val policyV = Validators.validateRequired(insurancePolicy, "La póliza de seguro")

    val isValid = listOf(brandV, modelV, yearV, plateV, fuelV, mileageV, chassisV, engineV, policyV).all { it.isValid }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle == null) "Agregar Vehículo" else "Editar Vehículo") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    DialogSectionLabel("Fotos y documentación")
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

                    if (vehicle == null) {
                        ExposedDropdownMenuBox(
                            expanded = expandedFuel,
                            onExpandedChange = { expandedFuel = !expandedFuel }
                        ) {
                            OutlinedTextField(
                                value = fuelType.label,
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
                    } else {
                        OutlinedTextField(
                            value = fuelType.label,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Tipo de Combustible") },
                            supportingText = { Text("No se modifica después del registro") },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                    DialogSectionLabel("Identificación del motor/chasis")
                    OutlinedTextField(
                        value = chassisNumber, onValueChange = { chassisNumber = it.uppercase() },
                        label = { Text("Número de Chasis") },
                        isError = attempted && !chassisV.isValid,
                        supportingText = if (attempted && !chassisV.isValid) {
                            { Text(chassisV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = engineNumber, onValueChange = { engineNumber = it.uppercase() },
                        label = { Text("Número de Motor") },
                        isError = attempted && !engineV.isValid,
                        supportingText = if (attempted && !engineV.isValid) {
                            { Text(engineV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    DialogSectionLabel("Seguro")
                    OutlinedTextField(
                        value = insurancePolicy, onValueChange = { insurancePolicy = it.uppercase() },
                        label = { Text("Número de Póliza de Seguro") },
                        isError = attempted && !policyV.isValid,
                        supportingText = if (attempted && !policyV.isValid) {
                            { Text(policyV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            fuelType = fuelType,
                            mileage = mileage.toLongOrNull() ?: 0L,
                            color = color.trim(),
                            chassisNumber = chassisNumber.trim(),
                            engineNumber = engineNumber.trim(),
                            insuranceExpiryDate = insuranceExpiry,
                            insurancePolicy = insurancePolicy.trim(),
                            circulationExpiryDate = vehicle?.circulationExpiryDate,
                            vehiclePhotoUri = vehiclePhotoUri,
                            registrationPhotoUri = registrationPhotoUri,
                            insurancePhotoUri = insurancePhotoUri,
                            status = vehicle?.status ?: VehicleStatus.AVAILABLE,
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
    assignmentHistory: List<AssignmentModel>,
    onDismiss: () -> Unit,
    onStatusChange: (VehicleStatus) -> Unit,
    onEdit: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var reportMaintenance by remember { mutableStateOf<Maintenance?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Vehículo") },
        text = {
            LazyColumn {
                item {
                    Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    StatusBadge(status = vehicle.status)
                    Spacer(modifier = Modifier.height(12.dp))
                    KeyValueRow("Placa", Validators.formatPlate(vehicle.plate), mono = true)
                    KeyValueRow("Año", "${vehicle.year}", mono = true)
                    KeyValueRow("Color", vehicle.color.ifBlank { "N/A" })
                    KeyValueRow("Combustible", vehicle.fuelType.label)
                    KeyValueRow("Kilometraje", "${vehicle.mileage} km", mono = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    DialogSectionLabel("Identificación")
                    KeyValueRow("Chasis", vehicle.chassisNumber.ifBlank { "N/A" }, mono = true)
                    KeyValueRow("Motor", vehicle.engineNumber.ifBlank { "N/A" }, mono = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    DialogSectionLabel("Documentación")
                    KeyValueRow("Póliza", vehicle.insurancePolicy.ifBlank { "N/A" }, mono = true)
                    KeyValueRow("Venc. Seguro", vehicle.insuranceExpiryDate?.let { sdf.format(it) } ?: "N/A")
                    Spacer(modifier = Modifier.height(8.dp))
                    KeyValueRow("Notas", vehicle.description.ifBlank { "Sin observaciones" })
                    Spacer(modifier = Modifier.height(16.dp))
                    DialogSectionLabel("Fotos")
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
                    DialogSectionLabel("Cambiar estado")
                    // Solo los estados que el admin puede setear a mano según el estado actual
                    // (espeja la máquina del backend). ASSIGNED/IN_USE/PENDING_REVIEW no van a mano.
                    val estadoTargets = manualVehicleTargets(vehicle.status)
                    if (estadoTargets.isEmpty()) {
                        Text(
                            "Estado gestionado por la asignación (no editable a mano).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            estadoTargets.forEach { status ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onStatusChange(status) },
                                    label = { Text(status.label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DialogSectionLabel("Historial de mantenimiento")
                }
                if (maintenanceHistory.isEmpty()) {
                    item { Text("No hay registros de mantenimiento.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(maintenanceHistory, key = { it.id }) { maintenance ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { reportMaintenance = maintenance }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("${maintenance.type.label} - ${sdf.format(maintenance.date)}")
                                Text("Estado: ${maintenance.status.label}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    DialogSectionLabel("Historial de asignaciones")
                }
                if (assignmentHistory.isEmpty()) {
                    item { Text("No hay registros de asignación.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(assignmentHistory, key = { it.id }) { assignment ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Salida: ${sdf.format(assignment.departureDate)}")
                                assignment.returnDate?.let {
                                    Text("Retorno: ${sdf.format(it)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Estado: ${assignment.status.label}", style = MaterialTheme.typography.bodySmall)                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Editar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )

    // Mini-informe del mantenimiento al tocar un item del historial.
    reportMaintenance?.let { m ->
        MaintenanceMiniReportDialog(maintenance = m, onDismiss = { reportMaintenance = null })
    }
}

/**
 * Estados de vehículo que el admin puede setear MANUALMENTE según el estado actual
 * (espeja TRANSICIONES de VehiculoService). ASSIGNED/IN_USE los maneja la asignación;
 * PENDING_REVIEW lo genera el "Reportar" del conductor.
 */
private fun manualVehicleTargets(current: VehicleStatus): List<VehicleStatus> = when (current) {
    VehicleStatus.AVAILABLE -> listOf(VehicleStatus.MAINTENANCE, VehicleStatus.OUT_OF_SERVICE, VehicleStatus.INACTIVE)
    VehicleStatus.PENDING_REVIEW -> listOf(VehicleStatus.AVAILABLE, VehicleStatus.MAINTENANCE, VehicleStatus.OUT_OF_SERVICE)
    VehicleStatus.MAINTENANCE -> listOf(VehicleStatus.AVAILABLE, VehicleStatus.OUT_OF_SERVICE, VehicleStatus.INACTIVE)
    VehicleStatus.OUT_OF_SERVICE -> listOf(VehicleStatus.AVAILABLE, VehicleStatus.MAINTENANCE, VehicleStatus.INACTIVE)
    VehicleStatus.INACTIVE -> listOf(VehicleStatus.AVAILABLE, VehicleStatus.OUT_OF_SERVICE)
    VehicleStatus.ASSIGNED, VehicleStatus.IN_USE -> emptyList()
}

/** Mini-informe de un mantenimiento (solo lectura), abierto desde el historial del vehículo. */
@Composable
private fun MaintenanceMiniReportDialog(maintenance: Maintenance, onDismiss: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(maintenance.type.label) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                StatusBadge(status = maintenance.status)
                Spacer(modifier = Modifier.height(12.dp))
                KeyValueRow("Inicio", sdf.format(maintenance.date))
                maintenance.completionDate?.let { KeyValueRow("Finalización", sdf.format(it)) }
                KeyValueRow("Kilometraje", "${maintenance.currentMileage} km", mono = true)
                KeyValueRow("Responsable", maintenance.responsible)
                maintenance.nextDate?.let { KeyValueRow("Próx. fecha", sdf.format(it)) }
                maintenance.nextMileage?.let { KeyValueRow("Próx. km", "$it km", mono = true) }
                Spacer(modifier = Modifier.height(8.dp))
                DialogSectionLabel("Descripción")
                Text(maintenance.description, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormDialog(
    maintenance: Maintenance?,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onSave: (Maintenance) -> Unit
) {
    val isEdit = maintenance != null
    var selectedVehicleId by remember { mutableStateOf(maintenance?.vehicleId ?: vehicles.firstOrNull()?.id ?: "") }
    var type by remember { mutableStateOf(maintenance?.type ?: MaintenanceType.PREVENTIVE) }
    var description by remember { mutableStateOf(maintenance?.description ?: "") }
    var responsible by remember { mutableStateOf(maintenance?.responsible ?: "") }

    var currentMileage by remember {
        mutableStateOf(
            maintenance?.currentMileage?.toString() ?:
            vehicles.find { it.id == selectedVehicleId }?.mileage?.toString() ?: ""
        )
    }
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
    val nextDateV = Validators.validateFutureDate(nextDate, "La próxima fecha", isEdit)

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
                                        onClick = {
                                            selectedVehicleId = v.id
                                            if (maintenance == null) {
                                                currentMileage = v.mileage.toString()
                                            }
                                            expandedVehicle = false
                                        }
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
                            supportingText = if (attempted && !mileageV.isValid) {
                                { Text(mileageV.errorMessage ?: "") }
                            } else null,
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
                            minDate = if (isEdit) null else Date(),
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
                                // Un mantenimiento nuevo arranca PENDIENTE; el admin lo avanza luego.
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
fun MaintenanceDetailsDialog(
    maintenance: Maintenance,
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Mantenimiento") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = maintenance.type.label, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(2.dp))
                StatusBadge(status = maintenance.status)
                Spacer(modifier = Modifier.height(12.dp))
                KeyValueRow("Vehículo", vehicle?.let { "${it.brand} ${it.model} (${Validators.formatPlate(it.plate)})" } ?: "Vehículo eliminado")
                KeyValueRow("Inicio", sdf.format(maintenance.date))
                maintenance.completionDate?.let { KeyValueRow("Finalización", sdf.format(it)) }
                KeyValueRow("Responsable", maintenance.responsible)
                KeyValueRow("Kilometraje", "${maintenance.currentMileage} km", mono = true)
                Spacer(modifier = Modifier.height(12.dp))
                DialogSectionLabel("Descripción")
                Text(text = maintenance.description, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Editar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
fun GeneralMaintenanceHistoryDialog(
    maintenances: List<Maintenance>,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onMaintenanceClick: (Maintenance) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historial General de Mantenimiento") },
        text = {
            if (maintenances.isEmpty()) {
                Text("No hay registros de mantenimiento.")
            } else {
                val sortedMaintenances = remember(maintenances) {
                    maintenances.sortedByDescending { it.date }
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(sortedMaintenances, key = { it.id }) { maintenance ->
                        val vehicle = vehicles.find { it.id == maintenance.vehicleId }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { onMaintenanceClick(maintenance) }
                        ) {
                            ListItem(
                                headlineContent = { Text("${maintenance.type.label} - ${vehicle?.let { "${it.brand} ${it.model}" } ?: "Vehículo eliminado"}") },
                                supportingContent = {
                                    Text("Fecha: ${sdf.format(maintenance.date)} | Estado: ${maintenance.status.label}")
                                },
                                trailingContent = {
                                    Icon(
                                        if (maintenance.status == MaintenanceStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = maintenance.status.statusColor                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
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

    var profilePhotoUri by remember { mutableStateOf(driver?.profilePhotoUri) }
    var licensePhotoUri by remember { mutableStateOf(driver?.licensePhotoUri) }

    val profilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> profilePhotoUri = uri?.toString() }

    val licensePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> licensePhotoUri = uri?.toString() }

    var attempted by remember { mutableStateOf(false) }

    val idRegistry = existingDrivers.map { it.id to it.idCardNumber }
    val phoneRegistry = existingDrivers.map { it.id to it.phone }
    val licenseRegistry = existingDrivers.map { it.id to it.licenseNumber }

    val firstNameV = Validators.validateName(firstName, "El nombre")
    val lastNameV = Validators.validateName(lastName, "El apellido")
    val idV = Validators.validateIdCard(idCardNumber, idRegistry, driver?.id)
    val ageV = Validators.validateAge(age)
    val phoneV = Validators.validatePhone(phone, phoneRegistry, driver?.id)
    val licenseV = Validators.validateLicenseNumber(licenseNumber, licenseRegistry, driver?.id)
    val expiryV = Validators.validateLicenseExpiry(licenseExpiry, driver != null)

    val isValid = listOf(firstNameV, lastNameV, idV, ageV, phoneV, licenseV, expiryV)
        .all { it.isValid }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (driver == null) "Agregar Conductor" else "Editar Conductor") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    DialogSectionLabel("Fotos")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        PhotoPlaceholder(
                            label = "Foto Perfil",
                            icon = Icons.Default.AddAPhoto,
                            uri = profilePhotoUri,
                            onPick = { profilePhotoLauncher.launch("image/*") }
                        )
                        PhotoPlaceholder(
                            label = "Foto Licencia",
                            icon = Icons.Default.CameraAlt,
                            uri = licensePhotoUri,
                            onPick = { licensePhotoLauncher.launch("image/*") }
                        )
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
                                else "Ej: 001-150385-0001A (con o sin guiones)"
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
                        value = licenseNumber, onValueChange = { licenseNumber = it.uppercase() },
                        label = { Text("Número de licencia") },
                        isError = attempted && !licenseV.isValid,
                        supportingText = if (attempted && !licenseV.isValid) {
                            { Text(licenseV.errorMessage ?: "") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "Vencimiento de licencia",
                        selectedDate = licenseExpiry,
                        onDateSelected = { licenseExpiry = it },
                        minDate = if (driver != null) null else Date(),
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
                            licenseNumber = Validators.normalizeIdCard(licenseNumber),
                            licenseExpiryDate = licenseExpiry ?: Date(),
                            profilePhotoUri = profilePhotoUri,
                            licensePhotoUri = licensePhotoUri,
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
fun DriverDetailsDialog(
    driver: Driver,
    onDismiss: () -> Unit,
    onStatusChange: (DriverStatus) -> Unit,
    onChangePassword: () -> Unit,
    onEdit: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Conductor") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .background(driver.effectiveStatus.statusColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person, contentDescription = null,
                            tint = driver.effectiveStatus.statusColor, modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = driver.fullName, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusBadge(status = driver.effectiveStatus)
                    }
                }
                if (driver.isLicenseExpired) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                                    "Licencia vencida",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "El conductor no puede ser asignado hasta que renueve su licencia.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                KeyValueRow("Cédula", driver.idCardNumber, mono = true)
                KeyValueRow("Edad", "${driver.age} años")
                KeyValueRow("Teléfono", driver.phone, mono = true)
                KeyValueRow("Licencia vence", sdf.format(driver.licenseExpiryDate))

                Spacer(modifier = Modifier.height(16.dp))
                DialogSectionLabel("Acceso")
                KeyValueRow("Usuario", driver.idCardNumber, mono = true)
                Text(
                    text = "La contraseña inicial es la cédula. El conductor puede cambiarla desde la app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onChangePassword) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar contraseña")
                }

                Spacer(modifier = Modifier.height(16.dp))
                DialogSectionLabel("Cambiar estado")
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
        confirmButton = { TextButton(onClick = onEdit) { Text("Editar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

/**
 * Credenciales iniciales del conductor recién creado. Usuario y contraseña = la cédula;
 * el conductor debería cambiar la contraseña en su primer ingreso.
 */
@Composable
fun DriverCredentialsDialog(driver: Driver, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Key, contentDescription = null) },
        title = { Text("Conductor creado") },
        text = {
            Column {
                Text(
                    text = "Se generó la cuenta de acceso. Compartile estas credenciales al conductor; " +
                            "debería cambiar la contraseña en su primer ingreso.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                KeyValueRow("Usuario", driver.idCardNumber, mono = true)
                KeyValueRow("Contraseña", driver.idCardNumber, mono = true)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendido") } }
    )
}

/** Diálogo para que el admin fije una nueva contraseña del conductor. */
@Composable
fun ChangePasswordDialog(
    driver: Driver,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val validation = Validators.validatePassword(password, confirm)
    val transformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                Text(
                    text = "Conductor: ${driver.fullName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = transformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = attempted && !validation.isValid,
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (visible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = transformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = attempted && !validation.isValid,
                    supportingText = if (attempted && !validation.isValid) {
                        { Text(validation.errorMessage ?: "") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (validation.isValid) onConfirm(password)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentFormDialog(
    assignment: AssignmentModel? = null,
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onSave: (AssignmentModel) -> Unit
) {
    val isEdit = assignment != null
    val noVehicles = vehicles.isEmpty() && !isEdit
    val noDrivers = drivers.isEmpty() && !isEdit
    val cannotProceed = noVehicles || noDrivers

    var selectedVehicleId by remember { mutableStateOf(assignment?.vehicleId ?: vehicles.firstOrNull()?.id ?: "") }
    var selectedDriverId by remember { mutableStateOf(assignment?.driverId ?: drivers.firstOrNull()?.id ?: "") }
    var initialMileage by remember { mutableStateOf(assignment?.initialMileage?.toString() ?: vehicles.find { it.id == selectedVehicleId }?.mileage?.toString() ?: "") }
    var departureDate by remember { mutableStateOf<Date?>(assignment?.departureDate ?: Date()) }
    var plannedReturnDate by remember {
        mutableStateOf<Date?>(assignment?.plannedReturnDate ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time)
    }
    var observations by remember { mutableStateOf(assignment?.departureObservations ?: "") }

    // Campos de retorno (solo edición)
    var status by remember { mutableStateOf(assignment?.status ?: AssignmentStatus.ACTIVE) }
    var returnDate by remember { mutableStateOf<Date?>(assignment?.returnDate ?: if (isEdit) Date() else null) }
    var finalMileage by remember { mutableStateOf(assignment?.finalMileage?.toString() ?: "") }
    var returnObservations by remember { mutableStateOf(assignment?.returnObservations ?: "") }

    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedDriver by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }

    val selectedVehicle = vehicles.find { it.id == selectedVehicleId }

    val mileageV = if (selectedVehicle != null)
        Validators.validateAssignmentInitialMileage(initialMileage, selectedVehicle.mileage)
    else
        Validators.validateMileage(initialMileage)

    val departureV = Validators.validateDepartureDate(departureDate, isEdit)
    val datesV = Validators.validateAssignmentDates(departureDate, plannedReturnDate)

    val finalMileageV = if (isEdit && status == AssignmentStatus.COMPLETED) {
        Validators.validateFinalMileage(finalMileage, assignment?.initialMileage ?: 0L)
    } else ValidationResult.Valid

    val isValid = !cannotProceed &&
            listOf(mileageV, departureV, datesV, finalMileageV).all { it.isValid } &&
            selectedVehicleId.isNotBlank() && selectedDriverId.isNotBlank() &&
            initialMileage.isNotBlank() && departureDate != null && plannedReturnDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (assignment == null) "Nueva Asignación" else "Editar Asignación") },
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
                        if (!isEdit) {
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
                        } else {
                            DialogSectionLabel("Información de asignación")
                            Text("ID Vehículo: ${assignment?.vehicleId}", style = MaterialTheme.typography.bodySmall)
                            Text("ID Conductor: ${assignment?.driverId}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(16.dp))
                            ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
                                OutlinedTextField(
                                    value = status.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Estado de la Asignación") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                                )
                                ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                                    // El admin solo transiciona manualmente entre Activa y Finalizada.
                                    // "Por aceptar"/"Rechazada" las maneja el flujo del conductor.
                                    listOf(AssignmentStatus.ACTIVE, AssignmentStatus.COMPLETED).forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s.label) },
                                            onClick = { status = s; expandedStatus = false }
                                        )
                                    }
                                }
                            }
                        }

                        DatePickerField(
                            label = "Fecha de salida",
                            selectedDate = departureDate,
                            onDateSelected = { departureDate = it },
                            minDate = if (isEdit) null else Date(),
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
                            readOnly = isEdit,
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

                        if (isEdit && status == AssignmentStatus.COMPLETED) {
                            Spacer(modifier = Modifier.height(24.dp))
                            DialogSectionLabel("Información de retorno")

                            DatePickerField(
                                label = "Fecha de retorno real",
                                selectedDate = returnDate,
                                onDateSelected = { returnDate = it },
                                minDate = departureDate
                            )

                            OutlinedTextField(
                                value = finalMileage,
                                onValueChange = { finalMileage = it.filter { c -> c.isDigit() } },
                                label = { Text("Kilometraje Final") },
                                isError = attempted && !finalMileageV.isValid,
                                supportingText = if (attempted && !finalMileageV.isValid) {
                                    { Text(finalMileageV.errorMessage ?: "") }
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = returnObservations,
                                onValueChange = { returnObservations = it },
                                label = { Text("Observaciones de Entrega") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    attempted = true
                    if (isValid) {
                        onSave(AssignmentModel(
                            id = assignment?.id ?: UUID.randomUUID().toString(),
                            vehicleId = selectedVehicleId,
                            driverId = selectedDriverId,
                            departureDate = departureDate ?: Date(),
                            plannedReturnDate = plannedReturnDate ?: Date(),
                            initialMileage = initialMileage.toLongOrNull() ?: 0L,
                            departureObservations = observations.trim(),
                            returnDate = if (status == AssignmentStatus.COMPLETED) returnDate else null,
                            finalMileage = if (status == AssignmentStatus.COMPLETED) finalMileage.toLongOrNull() else null,
                            returnObservations = returnObservations.trim(),
                            status = status
                        ))
                    }
                },
                enabled = !cannotProceed
            ) { Text(if (!isEdit) "Asignar" else "Guardar Cambios") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AssignmentDetailsDialog(
    assignment: AssignmentModel,
    vehicle: Vehicle?,
    driver: Driver?,
    onFetchPhoto: (id: String, initial: Boolean, onResult: (String?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var fotoSalida by remember { mutableStateOf<String?>(null) }
    var fotoEntrega by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(assignment.id) {
        if (assignment.hasPhotoInitial) onFetchPhoto(assignment.id, true) { fotoSalida = it }
        if (assignment.hasPhotoFinal) onFetchPhoto(assignment.id, false) { fotoEntrega = it }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles de la Asignación") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                StatusBadge(status = assignment.status)
                Spacer(modifier = Modifier.height(12.dp))
                KeyValueRow(
                    "Vehículo",
                    vehicle?.let { "${it.brand} ${it.model} (${Validators.formatPlate(it.plate)})" } ?: "Vehículo eliminado"
                )
                KeyValueRow("Conductor", driver?.fullName ?: "—")
                KeyValueRow("Salida", sdf.format(assignment.departureDate))
                KeyValueRow("Retorno planeado", sdfDate.format(assignment.plannedReturnDate))
                KeyValueRow("Km inicial", "${assignment.initialMileage}", mono = true)
                if (assignment.status == AssignmentStatus.COMPLETED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DialogSectionLabel("Retorno")
                    KeyValueRow("Entregado", assignment.returnDate?.let { sdf.format(it) } ?: "N/A")
                    KeyValueRow("Km final", assignment.finalMileage?.toString() ?: "N/A", mono = true)
                    if (assignment.returnObservations.isNotBlank()) {
                        KeyValueRow("Observaciones", assignment.returnObservations)
                    }
                }
                fotoSalida?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    DialogSectionLabel("Foto combustible (salida)")
                    Base64Image(it, Modifier.fillMaxWidth().height(180.dp))
                }
                fotoEntrega?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    DialogSectionLabel("Foto combustible (entrega)")
                    Base64Image(it, Modifier.fillMaxWidth().height(180.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onEdit) { Text("Editar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}