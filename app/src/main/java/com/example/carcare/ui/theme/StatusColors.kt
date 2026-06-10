package com.example.carcare.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.DriverStatus
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.VehicleStatus

/**
 * Fuente única de verdad para el color semántico de cada estado del dominio.
 *
 * Antes este mapeo vivía repetido (y con paletas distintas) en CommonComponents,
 * DashboardSection y StatsCard. Centralizarlo aquí garantiza que badges, KPIs y
 * gráficos pinten el mismo estado con el mismo color de la paleta de marca.
 */
val VehicleStatus.statusColor: Color
    get() = when (this) {
        VehicleStatus.AVAILABLE -> StatusAvailable
        VehicleStatus.ASSIGNED -> StatusAssigned
        VehicleStatus.IN_USE -> StatusInUse
        VehicleStatus.PENDING_REVIEW -> StatusPendingReview
        VehicleStatus.MAINTENANCE -> StatusMaintenance
        VehicleStatus.OUT_OF_SERVICE -> StatusOutOfService
        VehicleStatus.INACTIVE -> StatusInactive
    }

val DriverStatus.statusColor: Color
    get() = when (this) {
        DriverStatus.ACTIVE -> StatusAvailable
        DriverStatus.INACTIVE -> StatusInactive
        DriverStatus.SUSPENDED -> StatusOutOfService
    }

val MaintenanceStatus.statusColor: Color
    get() = when (this) {
        MaintenanceStatus.PENDING -> StatusPendingReview
        MaintenanceStatus.IN_PROGRESS -> StatusInUse
        MaintenanceStatus.COMPLETED -> StatusAvailable
    }

val AssignmentStatus.statusColor: Color
    get() = when (this) {
        AssignmentStatus.ACTIVE -> StatusInUse
        AssignmentStatus.COMPLETED -> StatusAvailable
    }