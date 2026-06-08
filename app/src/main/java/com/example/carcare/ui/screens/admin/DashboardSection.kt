package com.example.carcare.ui.screens.admin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carcare.model.*
import com.example.carcare.ui.theme.*
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*

/* ───────────────────────── Gráfico de barras ───────────────────────── */

data class Bar(val label: String, val value: Int, val color: Color)

/**
 * Gráfico de barras vertical, animado y calculado en vivo.
 * Sin librerías externas: Canvas + Animatable. Reescala solo según el valor máximo.
 */
@Composable
fun FleetBarChart(bars: List<Bar>, modifier: Modifier = Modifier, chartHeight: androidx.compose.ui.unit.Dp = 180.dp) {
    if (bars.isEmpty()) {
        Text("Sin datos para mostrar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val maxVal = (bars.maxOf { it.value }).coerceAtLeast(1)
    val progress = remember(bars) { Animatable(0f) }
    LaunchedEffect(bars) { progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }

    val gridColor = MaterialTheme.colorScheme.outline
    val valueColor = MaterialTheme.colorScheme.onSurface
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier.fillMaxWidth().height(chartHeight)) {
        val topPad = 20.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val plotH = size.height - topPad - bottomPad
        val n = bars.size
        val gap = 10.dp.toPx()
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(2f)

        // líneas de referencia
        val lines = 4
        for (i in 0..lines) {
            val y = topPad + plotH * i / lines
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
        }

        bars.forEachIndexed { i, b ->
            val x = i * (barW + gap)
            val h = plotH * (b.value.toFloat() / maxVal) * progress.value
            val top = topPad + plotH - h
            drawRoundRect(
                color = b.color,
                topLeft = Offset(x, top),
                size = Size(barW, h),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.apply {
                if (progress.value > 0.5f && b.value > 0) {
                    drawText(
                        b.value.toString(), x + barW / 2, top - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = valueColor.toArgb(); textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 11.sp.toPx(); isFakeBoldText = true; isAntiAlias = true
                        }
                    )
                }
                drawText(
                    b.label, x + barW / 2, size.height - 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = labelColor.toArgb(); textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 9.5.sp.toPx(); isAntiAlias = true
                    }
                )
            }
        }
    }
}

/* ───────────────────────── KPIs agrupados ───────────────────────── */

@Composable
private fun FleetHeroCard(total: Int, chips: List<Triple<String, Int, Color>>) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Petrol700)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.DirectionsCar, null, tint = Color.White) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(total.toString(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, fontFamily = FontFamily.Monospace)
                    Text("Vehículos en flota", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            FlowChips(chips)
        }
    }
}

@Composable
private fun FlowChips(chips: List<Triple<String, Int, Color>>) {
    // fila simple con wrap manual cada 3 para mantener compatibilidad sin FlowRow experimental
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chips.filter { it.second > 0 }.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (label, count, color) ->
                    Row(
                        Modifier.clip(RoundedCornerShape(9.dp)).background(Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(6.dp))
                        Text("$label ", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniKpi(
    value: String, caption: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color, badge: String? = null, badgeColor: Color = StatusAvailable, modifier: Modifier = Modifier
) {
    Card(modifier) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, fontFamily = FontFamily.Monospace)
                    Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (badge != null) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(badgeColor.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(badge, style = MaterialTheme.typography.labelSmall, color = badgeColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun statusColor(s: VehicleStatus) = when (s) {
    VehicleStatus.AVAILABLE -> StatusAvailable
    VehicleStatus.ASSIGNED -> StatusAssigned
    VehicleStatus.IN_USE -> StatusInUse
    VehicleStatus.PENDING_REVIEW -> StatusPendingReview
    VehicleStatus.MAINTENANCE -> StatusMaintenance
    VehicleStatus.OUT_OF_SERVICE -> StatusOutOfService
    VehicleStatus.INACTIVE -> StatusInactive
}

/* ───────────────────────── Dashboard ─────────────────────────
   Reemplaza al DashboardSection anterior (borra el viejo de AdminScreenSections.kt). */

@Composable
fun DashboardSection(
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    maintenances: List<Maintenance>,
    assignments: List<Assignment>
) {
    val now = remember { Date() }
    val soon = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time }

    val statusCount = remember(vehicles) { vehicles.groupingBy { it.status }.eachCount() }
    val activeDrivers = remember(drivers) { drivers.count { it.status == DriverStatus.ACTIVE } }
    val maintInProgress = remember(maintenances) { maintenances.count { it.status == MaintenanceStatus.IN_PROGRESS } }
    val activeAssignments = remember(assignments) { assignments.count { it.status == AssignmentStatus.ACTIVE } }
    val totalKm = remember(vehicles) { vehicles.sumOf { it.mileage } }

    val overdue = remember(maintenances, vehicles) {
        maintenances.count {
            it.status == MaintenanceStatus.IN_PROGRESS &&
                    ((it.nextDate != null && it.nextDate.before(now)) ||
                            (it.nextMileage != null && vehicles.find { v -> v.id == it.vehicleId }?.let { v -> v.mileage >= it.nextMileage } == true))
        }
    }

    val alerts = remember(maintenances, vehicles, drivers, now, soon) {
        val list = mutableListOf<Pair<Boolean, String>>() // (esCrítico, texto)
        maintenances.filter { it.status == MaintenanceStatus.IN_PROGRESS }.forEach { m ->
            val v = vehicles.find { it.id == m.vehicleId }
            val label = v?.let { "${it.brand} (${Validators.formatPlate(it.plate)})" } ?: "Vehículo"
            if (m.nextDate != null) {
                if (m.nextDate.before(now)) list.add(true to "Mantenimiento vencido · $label")
                else if (m.nextDate.before(soon)) list.add(false to "Mantenimiento próximo · $label")
            }
            if (m.nextMileage != null && v != null && v.mileage >= m.nextMileage) list.add(true to "Mant. vencido por KM · $label")
        }
        drivers.filter { it.status == DriverStatus.ACTIVE }.forEach { d ->
            if (d.licenseExpiryDate.before(now)) list.add(true to "Licencia vencida · ${d.fullName}")
            else if (d.licenseExpiryDate.before(soon)) list.add(false to "Licencia por vencer · ${d.fullName}")
        }
        list
    }

    val lastOut = remember(assignments) { assignments.sortedByDescending { it.departureDate }.take(3) }
    val lastIn = remember(assignments) { assignments.filter { it.status == AssignmentStatus.COMPLETED }.sortedByDescending { it.returnDate }.take(3) }

    val fleetChips = listOf(
        Triple("Disp.", statusCount[VehicleStatus.AVAILABLE] ?: 0, StatusAvailable),
        Triple("En uso", statusCount[VehicleStatus.IN_USE] ?: 0, StatusInUse),
        Triple("Asig.", statusCount[VehicleStatus.ASSIGNED] ?: 0, StatusAssigned),
        Triple("Mant.", statusCount[VehicleStatus.MAINTENANCE] ?: 0, StatusMaintenance),
        Triple("F. serv.", statusCount[VehicleStatus.OUT_OF_SERVICE] ?: 0, StatusOutOfService),
        Triple("Rev.", statusCount[VehicleStatus.PENDING_REVIEW] ?: 0, StatusPendingReview),
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SectionTitle("Resumen de flota")
            FleetHeroCard(total = vehicles.size, chips = fleetChips)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniKpi("$activeDrivers", "Conductores activos", Icons.Default.Person, StatusAvailable,
                    badge = "de ${drivers.size} totales", badgeColor = StatusAvailable, modifier = Modifier.weight(1f))
                MiniKpi("$maintInProgress", "Mant. en proceso", Icons.Default.Build, Amber,
                    badge = if (overdue > 0) "$overdue vencidos" else "al día",
                    badgeColor = if (overdue > 0) StatusOutOfService else StatusAvailable, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniKpi("$activeAssignments", "Asignaciones activas", Icons.AutoMirrored.Filled.Assignment, StatusInUse,
                    badge = "en circulación", badgeColor = StatusInUse, modifier = Modifier.weight(1f))
                MiniKpi("${totalKm / 1000}k", "Km totales de flota", Icons.Default.Speed, Petrol700,
                    badge = "prom. ${if (vehicles.isNotEmpty()) totalKm / vehicles.size / 1000 else 0}k km",
                    badgeColor = AmberDeep, modifier = Modifier.weight(1f))
            }
        }

        item {
            SectionTitle("Estadísticas")
            val statGroups = remember(vehicles, drivers, maintenances, assignments) {
                buildDashboardStats(vehicles, drivers, maintenances, assignments)
            }
            StatsCard(groups = statGroups)
        }

        if (alerts.isNotEmpty()) {
            item { SectionTitle("Alertas críticas") }
            items(alerts) { (critical, text) ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (critical) StatusOutOfService.copy(alpha = 0.10f)
                        else Amber.copy(alpha = 0.12f))
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = if (critical) StatusOutOfService else AmberDeep)
                        Spacer(Modifier.width(10.dp))
                        Text(text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (lastOut.isNotEmpty()) {
            item { SectionTitle("Últimas salidas") }
            items(lastOut) { a -> ActivityRow(a, vehicles, drivers, out = true) }
        }
        if (lastIn.isNotEmpty()) {
            item { SectionTitle("Últimas entregas") }
            items(lastIn) { a -> ActivityRow(a, vehicles, drivers, out = false) }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun ActivityRow(a: Assignment, vehicles: List<Vehicle>, drivers: List<Driver>, out: Boolean) {
    val v = vehicles.find { it.id == a.vehicleId }
    val d = drivers.find { it.id == a.driverId }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text("${v?.brand} ${v?.model} (${v?.plate?.let { Validators.formatPlate(it) }})") },
            supportingContent = {
                Text(
                    "Conductor: ${d?.fullName}\n" +
                            if (out) "Salida: ${sdf.format(a.departureDate)}" else "Entregado: ${a.returnDate?.let { sdf.format(it) }}"
                )
            },
            leadingContent = {
                if (out) Icon(Icons.AutoMirrored.Filled.Logout, null, tint = StatusOutOfService)
                else Icon(Icons.AutoMirrored.Filled.Login, null, tint = StatusAvailable)
            }
        )
    }
}