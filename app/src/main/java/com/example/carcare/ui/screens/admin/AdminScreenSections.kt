package com.example.carcare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.Driver
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.Vehicle
import com.example.carcare.ui.components.EmptyState
import com.example.carcare.ui.components.LeadingTile
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.ui.components.pressScale
import com.example.carcare.ui.theme.instrumentSmall
import com.example.carcare.ui.theme.statusColor
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VehicleListSection(
    vehicles: List<Vehicle>,
    onVehicleClick: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit
) {
    if (vehicles.isEmpty()) {
        EmptyState(
            icon = Icons.Default.DirectionsCar,
            title = "Sin vehículos",
            subtitle = "Agregá el primer vehículo de la flota con el botón +.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(vehicles, key = { it.id }) { vehicle ->
                Box(Modifier.animateItem()) {
                    VehicleItem(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle) },
                        onDelete = { onDelete(vehicle) }
                    )
                }
            }
        }
    }
}

@Composable
fun MaintenanceListSection(
    maintenances: List<Maintenance>,
    vehicles: List<Vehicle>,
    onClick: (Maintenance) -> Unit,
    onDelete: (Maintenance) -> Unit,
    onStatusChange: (Maintenance, MaintenanceStatus) -> Unit
) {
    if (maintenances.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Build,
            title = "Taller al día",
            subtitle = "No hay mantenimientos pendientes. Registrá uno con el botón +.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(maintenances, key = { it.id }) { maintenance ->
                val vehicle = vehicles.find { it.id == maintenance.vehicleId }
                Box(Modifier.animateItem()) {
                    MaintenanceItem(
                        maintenance = maintenance,
                        vehicle = vehicle,
                        onClick = { onClick(maintenance) },
                        onDelete = { onDelete(maintenance) },
                        onStatusChange = { onStatusChange(maintenance, it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceItem(
    maintenance: Maintenance,
    vehicle: Vehicle?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (MaintenanceStatus) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val plateLabel = vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"
    val interaction = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).pressScale(interaction),
        onClick = onClick,
        interactionSource = interaction
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LeadingTile(icon = Icons.Default.Build, tint = maintenance.status.statusColor)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = maintenance.type.label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        text = vehicle?.let { "${it.brand} ${it.model} · $plateLabel" } ?: "Vehículo eliminado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Inicio: ${sdf.format(maintenance.date)}" +
                                (maintenance.completionDate?.let { " · Fin: ${sdf.format(it)}" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RowActions(onDelete = onDelete)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MaintenanceStatus.entries.forEach { status ->
                    FilterChip(
                        selected = maintenance.status == status,
                        onClick = { onStatusChange(status) },
                        label = { Text(status.label, style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
            if (maintenance.nextDate != null || maintenance.nextMileage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val nextDateStr = maintenance.nextDate?.let { sdf.format(it) } ?: "N/A"
                val nextKmStr = maintenance.nextMileage?.let { "$it km" } ?: "N/A"
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Próximo: $nextDateStr o $nextKmStr",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DriverListSection(
    drivers: List<Driver>,
    onDriverClick: (Driver) -> Unit,
    onDelete: (Driver) -> Unit
) {
    if (drivers.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Person,
            title = "Sin conductores",
            subtitle = "Agregá conductores para poder asignarles vehículos.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(drivers, key = { it.id }) { driver ->
                Box(Modifier.animateItem()) {
                    DriverItem(
                        driver = driver,
                        onClick = { onDriverClick(driver) },
                        onDelete = { onDelete(driver) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverItem(
    driver: Driver,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).pressScale(interaction),
        onClick = onClick,
        interactionSource = interaction,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape)
                    .background(driver.status.statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, contentDescription = null,
                    tint = driver.status.statusColor, modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = driver.fullName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    text = driver.idCardNumber,
                    style = instrumentSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(status = driver.status)
            }
            RowActions(onDelete = onDelete)
        }
    }
}

@Composable
fun AssignmentListSection(
    assignments: List<Assignment>,
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onClick: (Assignment) -> Unit,
    onDelete: (Assignment) -> Unit
) {
    if (assignments.isEmpty()) {
        EmptyState(
            icon = Icons.AutoMirrored.Filled.Assignment,
            title = "Sin asignaciones",
            subtitle = "Asigná un vehículo disponible a un conductor con el botón +.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(assignments, key = { it.id }) { assignment ->
                val vehicle = vehicles.find { it.id == assignment.vehicleId }
                val driver = drivers.find { it.id == assignment.driverId }
                Box(Modifier.animateItem()) {
                    AssignmentItem(
                        assignment = assignment,
                        vehicle = vehicle,
                        driver = driver,
                        onClick = { onClick(assignment) },
                        onDelete = { onDelete(assignment) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentItem(
    assignment: Assignment,
    vehicle: Vehicle?,
    driver: Driver?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val plateLabel = vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"
    val interaction = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).pressScale(interaction),
        onClick = onClick,
        interactionSource = interaction
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LeadingTile(icon = Icons.Default.DirectionsCar, tint = assignment.status.statusColor)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = vehicle?.let { "${it.brand} ${it.model} · $plateLabel" } ?: "Vehículo eliminado",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = "Conductor: ${driver?.fullName ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatusBadge(status = assignment.status)
                }
                RowActions(onDelete = onDelete)
            }
            Spacer(modifier = Modifier.height(10.dp))
            DetailLine("Salida", sdf.format(assignment.departureDate))
            DetailLine("Retorno planeado", sdfDate.format(assignment.plannedReturnDate))
            DetailLine("Km inicial", "${assignment.initialMileage}", mono = true)
            if (assignment.status == AssignmentStatus.COMPLETED) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailLine("Entregado", assignment.returnDate?.let { sdf.format(it) } ?: "N/A")
                DetailLine("Km final", "${assignment.finalMileage}", mono = true)
                if (assignment.returnObservations.isNotBlank()) {
                    DetailLine("Obs.", assignment.returnObservations)
                }
            }
        }
    }
}

/** Par etiqueta/valor alineado, con el valor opcionalmente en monospace (km, números). */
@Composable
private fun DetailLine(label: String, value: String, mono: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(124.dp)
        )
        Text(
            text = value,
            style = if (mono) instrumentSmall else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
