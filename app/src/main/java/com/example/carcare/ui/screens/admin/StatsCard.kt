package com.example.carcare.ui.screens.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carcare.model.*
import com.example.carcare.ui.theme.*
import com.example.carcare.util.Validators
import java.text.SimpleDateFormat
import java.util.*

/* ───────────────────────── Modelo de las estadísticas ───────────────────────── */

/** Una estadística: etiqueta corta (chip), título completo, total para el badge y sus barras. */
data class StatEntry(val label: String, val title: String, val total: Int, val bars: List<Bar>)

/** Un grupo = una entidad (Vehículos, Conductores…) con sus estadísticas. */
data class StatGroup(val entity: String, val icon: ImageVector, val stats: List<StatEntry>)

/* ───────────────────────── Tarjeta de estadísticas (2 niveles) ───────────────────────── */

/**
 * Selector de dos niveles: arriba la entidad, abajo la estadística.
 * El gráfico cambia con AnimatedContent (crossfade) y las barras vuelven a animarse.
 */
@Composable
fun StatsCard(groups: List<StatGroup>, modifier: Modifier = Modifier) {
    if (groups.isEmpty()) return
    var entityIdx by rememberSaveable { mutableStateOf(0) }
    var statIdx by rememberSaveable { mutableStateOf(0) }

    val safeEntity = entityIdx.coerceIn(0, groups.lastIndex)
    val group = groups[safeEntity]
    val safeStat = statIdx.coerceIn(0, group.stats.lastIndex)
    val stat = group.stats[safeStat]

    Card(modifier.fillMaxWidth().padding(top = 12.dp)) {
        Column(Modifier.padding(16.dp)) {

            // ── Nivel 1: entidad (scroll horizontal, selección con color animado) ──
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groups.forEachIndexed { i, g ->
                    val active = i == safeEntity
                    val bg by animateColorAsState(
                        if (active) Petrol700 else MaterialTheme.colorScheme.surfaceVariant,
                        tween(220), label = "entityBg"
                    )
                    val fg by animateColorAsState(
                        if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        tween(220), label = "entityFg"
                    )
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .clickable { entityIdx = i; statIdx = 0 }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(g.icon, null, tint = fg, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(g.entity, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Nivel 2: estadística (entra con fade + slide al cambiar de entidad) ──
            AnimatedContent(
                targetState = safeEntity,
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInHorizontally(tween(180)) { it / 8 }) togetherWith fadeOut(tween(100))
                },
                label = "statChips"
            ) { idx ->
                val stats = groups[idx].stats
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stats.forEachIndexed { i, s ->
                        val active = i == statIdx.coerceIn(0, stats.lastIndex)
                        val cbg by animateColorAsState(if (active) Petrol100 else Color.Transparent, tween(180), label = "chipBg")
                        val cfg by animateColorAsState(if (active) Petrol900 else MaterialTheme.colorScheme.onSurfaceVariant, tween(180), label = "chipFg")
                        val cborder = if (active) Petrol100 else MaterialTheme.colorScheme.outline
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .border(1.dp, cborder, RoundedCornerShape(50))
                                .background(cbg)
                                .clickable { statIdx = i }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(s.label, color = cfg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Gráfico (crossfade + barras que vuelven a crecer) ──
            AnimatedContent(
                targetState = stat,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 12 }) togetherWith fadeOut(tween(120))
                },
                label = "statChart"
            ) { s ->
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        CountBadge(s.total)
                    }
                    Spacer(Modifier.height(4.dp))
                    FleetBarChart(s.bars, chartHeight = 170.dp)
                }
            }
        }
    }
}

@Composable
private fun CountBadge(n: Int) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(n.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

/* ───────────────────────── Cálculo de las 14 estadísticas ───────────────────────── */

/**
 * Construye las estadísticas agrupadas por entidad, en vivo desde los datos.
 * Ventana "por vencer" = 30 días (ajustable). "Top" = 6. "Por mes" = últimos 6 meses.
 */
fun buildDashboardStats(
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    maintenances: List<Maintenance>,
    assignments: List<Assignment>
): List<StatGroup> {
    val now = Date()
    val soon = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
    val palette = listOf(Petrol700, StatusAssigned, StatusInUse, Amber, StatusPendingReview, Petrol300)

    // ---- Vehículos ----
    val vEstado = VehicleStatus.entries.mapNotNull { s ->
        val c = vehicles.count { it.status == s }
        if (c > 0) Bar(s.label, c, vehColor(s)) else null
    }
    val vSeguros = expiryBars(vehicles.map { it.insuranceExpiryDate }, now, soon)
    val vKm = listOf(
        Bar("0–50k", vehicles.count { it.mileage < 50_000 }, Petrol300),
        Bar("50–100k", vehicles.count { it.mileage in 50_000 until 100_000 }, StatusAssigned),
        Bar("+100k", vehicles.count { it.mileage >= 100_000 }, Petrol700)
    ).filter { it.value > 0 }

    // ---- Conductores ----
    val dEstado = DriverStatus.entries.mapNotNull { s ->
        val c = drivers.count { it.effectiveStatus == s }
        if (c > 0) Bar(s.label, c, driverColor(s)) else null
    }
    // Un conductor cuenta como "asignado" con carrera activa o pendiente de aceptar (reservado).
    val asignadosIds = assignments
        .filter { it.status == AssignmentStatus.ACTIVE || it.status == AssignmentStatus.PENDING_ACCEPTANCE }
        .map { it.driverId }.toSet()
    val asign = drivers.count { it.id in asignadosIds }
    val dDisp = listOf(
        Bar("Asignados", asign, StatusInUse),
        Bar("Libres", drivers.size - asign, StatusAvailable)
    ).filter { it.value > 0 }
    val dLic = expiryBars(drivers.map { it.licenseExpiryDate }, now, soon)

    // ---- Mantenimientos ----
    val mTipo = maintenances.groupingBy { it.type }.eachCount().entries
        .sortedByDescending { it.value }.take(6)
        .mapIndexed { i, e -> Bar(e.key.label, e.value, palette[i % palette.size]) }
    val mEstado = MaintenanceStatus.entries.mapNotNull { s ->
        val c = maintenances.count { it.status == s }
        if (c > 0) Bar(s.label, c, maintColor(s)) else null
    }
    val mPorVeh = maintenances.groupingBy { it.vehicleId }.eachCount().entries
        .sortedByDescending { it.value }.take(6)
        .mapIndexed { i, e ->
            val v = vehicles.find { it.id == e.key }
            Bar(v?.let { Validators.formatPlate(it.plate) } ?: "—", e.value, palette[i % palette.size])
        }
    val mMes = monthlyBars(maintenances.map { it.date }, Petrol700)

    // ---- Asignaciones ----
    val aEstado = AssignmentStatus.entries.mapNotNull { s ->
        val c = assignments.count { it.status == s }
        if (c > 0) Bar(assignLabel(s), c, assignColor(s)) else null
    }
    val aVehUsados = assignments.groupingBy { it.vehicleId }.eachCount().entries
        .sortedByDescending { it.value }.take(6)
        .mapIndexed { i, e ->
            val v = vehicles.find { it.id == e.key }
            Bar(v?.let { Validators.formatPlate(it.plate) } ?: "—", e.value, palette[i % palette.size])
        }
    val aPorCond = assignments.groupingBy { it.driverId }.eachCount().entries
        .sortedByDescending { it.value }.take(6)
        .mapIndexed { i, e ->
            val d = drivers.find { it.id == e.key }
            Bar(d?.firstName ?: "—", e.value, palette[i % palette.size])
        }
    val aMes = monthlyBars(assignments.map { it.departureDate }, StatusInUse)

    return listOf(
        StatGroup("Vehículos", Icons.Default.DirectionsCar, listOf(
            StatEntry("Estado", "Vehículos por estado", vehicles.size, vEstado),
            StatEntry("Seguros", "Seguros de vehículos", vehicles.size, vSeguros),
            StatEntry("Kilometraje", "Vehículos por kilometraje", vehicles.size, vKm)
        )),
        StatGroup("Conductores", Icons.Default.Person, listOf(
            StatEntry("Estado", "Conductores por estado", drivers.size, dEstado),
            StatEntry("Disponibilidad", "Disponibilidad de conductores", drivers.size, dDisp),
            StatEntry("Licencia", "Licencias por vencimiento", drivers.size, dLic)
        )),
        StatGroup("Mantenimientos", Icons.Default.Build, listOf(
            StatEntry("Tipo", "Mantenimientos por tipo", maintenances.size, mTipo),
            StatEntry("Estado", "Mantenimientos por estado", maintenances.size, mEstado),
            StatEntry("Por vehículo", "Mantenimientos por vehículo", maintenances.size, mPorVeh),
            StatEntry("Por mes", "Mantenimientos por mes", maintenances.size, mMes)
        )),
        StatGroup("Asignaciones", Icons.AutoMirrored.Filled.Assignment, listOf(
            StatEntry("Estado", "Asignaciones por estado", assignments.size, aEstado),
            StatEntry("Más usados", "Vehículos más usados", assignments.size, aVehUsados),
            StatEntry("Por conductor", "Usos por conductor", assignments.size, aPorCond),
            StatEntry("Por mes", "Asignaciones por mes", assignments.size, aMes)
        ))
    )
}

/* ── Helpers ── */

private fun expiryBars(dates: List<Date?>, now: Date, soon: Date): List<Bar> {
    var vencido = 0; var porVencer = 0; var vigente = 0; var sinDato = 0
    dates.forEach { d ->
        when {
            d == null -> sinDato++
            d.before(now) -> vencido++
            d.before(soon) -> porVencer++
            else -> vigente++
        }
    }
    return listOf(
        Bar("Vigente", vigente, StatusAvailable),
        Bar("Por vencer", porVencer, Amber),
        Bar("Vencido", vencido, StatusOutOfService),
        Bar("Sin dato", sinDato, StatusInactive)
    ).filter { it.value > 0 }
}

private fun monthlyBars(dates: List<Date>, color: Color, months: Int = 6): List<Bar> {
    val fmt = SimpleDateFormat("MMM", Locale("es"))
    val iter = Calendar.getInstance().apply { add(Calendar.MONTH, -(months - 1)) }
    val keys = ArrayList<String>(months)
    val labels = ArrayList<String>(months)
    repeat(months) {
        keys.add("${iter.get(Calendar.YEAR)}-${iter.get(Calendar.MONTH)}")
        labels.add(fmt.format(iter.time).replaceFirstChar { it.uppercase() })
        iter.add(Calendar.MONTH, 1)
    }
    val counts = HashMap<String, Int>()
    val tmp = Calendar.getInstance()
    dates.forEach { d ->
        tmp.time = d
        val k = "${tmp.get(Calendar.YEAR)}-${tmp.get(Calendar.MONTH)}"
        counts[k] = (counts[k] ?: 0) + 1
    }
    return keys.indices.map { Bar(labels[it], counts[keys[it]] ?: 0, color) }
}

private fun vehColor(s: VehicleStatus) = when (s) {
    VehicleStatus.AVAILABLE -> StatusAvailable
    VehicleStatus.ASSIGNED -> StatusAssigned
    VehicleStatus.IN_USE -> StatusInUse
    VehicleStatus.PENDING_REVIEW -> StatusPendingReview
    VehicleStatus.MAINTENANCE -> StatusMaintenance
    VehicleStatus.OUT_OF_SERVICE -> StatusOutOfService
    VehicleStatus.INACTIVE -> StatusInactive
}

private fun driverColor(s: DriverStatus) = when (s) {
    DriverStatus.ACTIVE -> StatusAvailable
    DriverStatus.INACTIVE -> StatusInactive
    DriverStatus.SUSPENDED -> StatusOutOfService
}

private fun maintColor(s: MaintenanceStatus) = when (s) {
    MaintenanceStatus.PENDING -> StatusPendingReview
    MaintenanceStatus.IN_PROGRESS -> StatusInUse
    MaintenanceStatus.COMPLETED -> StatusAvailable
}

private fun assignColor(s: AssignmentStatus) = when (s) {
    AssignmentStatus.PENDING_ACCEPTANCE -> StatusAssigned
    AssignmentStatus.ACTIVE -> StatusInUse
    AssignmentStatus.COMPLETED -> StatusAvailable
    AssignmentStatus.REJECTED -> StatusOutOfService
}

private fun assignLabel(s: AssignmentStatus) = s.label