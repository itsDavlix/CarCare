package com.example.carcare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.Driver
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import com.example.carcare.model.MaintenanceType
import com.example.carcare.ui.components.StatusBadge
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VehicleListSection(
    vehicles: List<Vehicle>,
    onVehicleClick: (Vehicle) -> Unit,
    onEdit: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(vehicles, key = { it.id }) { vehicle ->
            Box(Modifier.animateItem()) {
                VehicleItem(
                    vehicle = vehicle,
                    onClick = { onVehicleClick(vehicle) },
                    onEdit = { onEdit(vehicle) },
                    onDelete = { onDelete(vehicle) }
                )
            }
        }
    }
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(maintenances, key = { it.id }) { maintenance ->
                val vehicle = vehicles.find { it.id == maintenance.vehicleId }
                Box(Modifier.animateItem()) {
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
}

@Composable
fun MaintenanceItem(
    maintenance: Maintenance,
    vehicle: Vehicle?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (MaintenanceStatus) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val plateLabel = vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = maintenance.type.label, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Vehículo: ${vehicle?.let { "${it.brand} ${it.model} ($plateLabel)" } ?: "Vehículo eliminado"}")
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(drivers, key = { it.id }) { driver ->
                Box(Modifier.animateItem()) {
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
                StatusBadge(status = driver.status)
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
fun AssignmentListSection(
    assignments: List<Assignment>,
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    onEdit: (Assignment) -> Unit,
    onDelete: (Assignment) -> Unit
) {
    if (assignments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay asignaciones activas.")
        }
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
                        onEdit = { onEdit(assignment) },
                        onDelete = { onDelete(assignment) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentItem(
    assignment: Assignment,
    vehicle: Vehicle?,
    driver: Driver?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val plateLabel = vehicle?.plate?.let { Validators.formatPlate(it) } ?: "N/A"
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Vehículo: ${vehicle?.let { "${it.brand} ${it.model} ($plateLabel)" } ?: "Vehículo eliminado"}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Conductor: ${driver?.fullName}")
                    Text(text = "Salida: ${sdf.format(assignment.departureDate)}")
                    Text(text = "Retorno planeado: ${sdfDate.format(assignment.plannedReturnDate)}")
                    Text(text = "Km Inicial: ${assignment.initialMileage}")
                    StatusBadge(status = assignment.status)
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
            if (assignment.status == AssignmentStatus.COMPLETED) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Entregado: ${assignment.returnDate?.let { sdf.format(it) }}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Km Final: ${assignment.finalMileage}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Obs: ${assignment.returnObservations}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun VehicleStatusDistributionChart(vehicles: List<Vehicle>) {
    val total = vehicles.size.coerceAtLeast(1)
    val distribution = remember(vehicles) {
        VehicleStatus.entries.map { status ->
            status to vehicles.count { it.status == status }
        }.filter { it.second > 0 }
    }

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