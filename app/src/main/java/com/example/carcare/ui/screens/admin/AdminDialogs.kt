package com.example.carcare.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.model.*
import com.example.carcare.ui.components.DatePickerField
import com.example.carcare.util.ValidationResult
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*

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
    var insuranceExpiry by remember { mutableStateOf(vehicle?.insuranceExpiryDate) }

    var fuelType by remember { mutableStateOf(vehicle?.fuelType ?: FuelType.GASOLINE) }
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
    val fuelV = Validators.validateFuelType(fuelType.label)
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
                            fuelType = fuelType,
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
                    Text("Combustible: ${vehicle.fuelType.label}")
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
                                status = maintenance?.status ?: MaintenanceStatus.IN_PROGRESS
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
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Mantenimiento") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Tipo: ${maintenance.type.label}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Vehículo: ${vehicle?.brand} ${vehicle?.model} (${vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"})")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Fecha Inicio: ${sdf.format(maintenance.date)}")
                maintenance.completionDate?.let {
                    Text(text = "Fecha Finalización: ${sdf.format(it)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Responsable: ${maintenance.responsible}")
                Text(text = "Kilometraje: ${maintenance.currentMileage} km")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Descripción:", style = MaterialTheme.typography.labelMedium)
                Text(text = maintenance.description)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Estado: ${maintenance.status.label}", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
fun GeneralMaintenanceHistoryDialog(
    maintenances: List<Maintenance>,
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onMaintenanceClick: (Maintenance) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historial General de Mantenimiento") },
        text = {
            if (maintenances.isEmpty()) {
                Text("No hay registros de mantenimiento.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(maintenances.sortedByDescending { it.date }) { maintenance ->
                        val vehicle = vehicles.find { it.id == maintenance.vehicleId }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { onMaintenanceClick(maintenance) }
                        ) {
                            ListItem(
                                headlineContent = { Text("${maintenance.type.label} - ${vehicle?.brand} ${vehicle?.model}") },
                                supportingContent = { 
                                    Text("Fecha: ${sdf.format(maintenance.date)} | Estado: ${maintenance.status.label}")
                                },
                                trailingContent = {
                                    Icon(
                                        if (maintenance.status == MaintenanceStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (maintenance.status == MaintenanceStatus.COMPLETED) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                    )
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

    // Fotos: ahora funcionales (antes eran decorativas)
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
    val expiryV = Validators.validateLicenseExpiry(licenseExpiry)

    val isValid = listOf(firstNameV, lastNameV, idV, ageV, phoneV, licenseV, expiryV)
        .all { it.isValid }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (driver == null) "Agregar Conductor" else "Editar Conductor") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("Fotos", style = MaterialTheme.typography.labelMedium)
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
