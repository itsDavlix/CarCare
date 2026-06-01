package com.example.carcare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.model.*
import com.example.carcare.ui.components.DriverStatusBadge
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardSection(
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    maintenances: List<Maintenance>,
    assignments: List<Assignment>
) {
    val now = remember { Date() }
    val soon = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time }

    val stats = remember(vehicles, drivers, maintenances, assignments) {
        listOf(
            StatData("Total Vehículos", vehicles.size.toString(), Icons.Default.DirectionsCar, Color.Gray),
            StatData("Disponibles", vehicles.count { it.status == VehicleStatus.AVAILABLE }.toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50)),
            StatData("Asignados", vehicles.count { it.status == VehicleStatus.ASSIGNED }.toString(), Icons.Default.AssignmentInd, Color(0xFF9C27B0)),
            StatData("En Uso", vehicles.count { it.status == VehicleStatus.IN_USE }.toString(), Icons.Default.LocalShipping, Color(0xFF2196F3)),
            StatData("En Mantenimiento", vehicles.count { it.status == VehicleStatus.MAINTENANCE }.toString(), Icons.Default.Build, Color(0xFFFF9800)),
            StatData("Fuera de Servicio", vehicles.count { it.status == VehicleStatus.OUT_OF_SERVICE }.toString(), Icons.Default.Warning, Color(0xFFF44336)),
            StatData("Conductores Activos", drivers.count { it.status == DriverStatus.ACTIVE }.toString(), Icons.Default.Person, Color(0xFF4CAF50)),
            StatData("Conductores Inactivos", drivers.count { it.status == DriverStatus.INACTIVE }.toString(), Icons.Default.PersonOff, Color(0xFF9E9E9E)),
            StatData("Mant. En Proceso", maintenances.count { it.status == MaintenanceStatus.IN_PROGRESS }.toString(), Icons.Default.Schedule, Color(0xFFFF9800)),
            StatData("Asignaciones Activas", assignments.count { it.status == AssignmentStatus.ACTIVE }.toString(), Icons.AutoMirrored.Filled.Assignment, Color(0xFF3F51B5))
        )
    }

    val overdueMaintenances = remember(maintenances, vehicles) {
        maintenances.count { it.status == MaintenanceStatus.IN_PROGRESS &&
                ((it.nextDate != null && it.nextDate.before(now)) ||
                        (it.nextMileage != null && vehicles.find { v -> v.id == it.vehicleId }?.let { v -> v.mileage >= it.nextMileage } == true))
        }
    }

    val extraStats = remember(overdueMaintenances, vehicles) {
        listOf(
            StatData("Mant. Vencidos", overdueMaintenances.toString(), Icons.Default.RunningWithErrors, Color.Red),
            StatData("Pend. Revisión", vehicles.count { it.status == VehicleStatus.PENDING_REVIEW }.toString(), Icons.AutoMirrored.Filled.FactCheck, Color(0xFF795548))
        )
    }

    val alerts = remember(maintenances, vehicles, drivers, now, soon) {
        val list = mutableListOf<String>()

        maintenances.filter { it.status == MaintenanceStatus.IN_PROGRESS }.forEach { m ->
            val vehicle = vehicles.find { it.id == m.vehicleId }
            val vehicleLabel = vehicle?.let { "${it.brand} (${Validators.formatPlate(it.plate)})" } ?: "Vehículo"

            if (m.nextDate != null) {
                if (m.nextDate.before(now)) list.add("VENCIDO: Mantenimiento $vehicleLabel")
                else if (m.nextDate.before(soon)) list.add("PRÓXIMO: Mantenimiento $vehicleLabel")
            }

            if (m.nextMileage != null && vehicle != null && vehicle.mileage >= m.nextMileage) {
                list.add("VENCIDO (KM): Mantenimiento $vehicleLabel")
            }
        }

        drivers.filter { it.status == DriverStatus.ACTIVE }.forEach { d ->
            if (d.licenseExpiryDate.before(now)) list.add("VENCIDO: Licencia de ${d.fullName}")
            else if (d.licenseExpiryDate.before(soon)) list.add("PRÓXIMO: Venc. Licencia ${d.fullName}")
        }
        list
    }

    val lastCheckOuts = remember(assignments) { assignments.sortedByDescending { it.departureDate }.take(3) }
    val lastCheckIns = remember(assignments) { assignments.filter { it.status == AssignmentStatus.COMPLETED }.sortedByDescending { it.returnDate }.take(3) }

    val totalKm = remember(vehicles) { vehicles.sumOf { it.mileage } }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Estadísticas de Flota", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            val statsToDisplay = stats + extraStats
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in statsToDisplay.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(statsToDisplay[i], modifier = Modifier.weight(1f))
                        if (i + 1 < statsToDisplay.size) {
                            StatCard(statsToDisplay[i + 1], modifier = Modifier.weight(1f))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
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
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }
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
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color(0xFF4CAF50)) }
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

@Composable
fun VehicleListSection(
    vehicles: List<Vehicle>,
    onVehicleClick: (Vehicle) -> Unit,
    onEdit: (Vehicle) -> Unit,
    onDelete: (Vehicle) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(vehicles, key = { it.id }) { vehicle ->
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(assignments, key = { it.id }) { assignment ->
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