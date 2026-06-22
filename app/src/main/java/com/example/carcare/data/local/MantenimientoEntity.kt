package com.example.carcare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.MaintenanceType
import java.util.Date

@Entity(tableName = "mantenimientos_cache")
data class MantenimientoEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val type: String,
    val date: Long,
    val completionDate: Long?,
    val currentMileage: Long,
    val description: String,
    val responsible: String,
    val nextDate: Long?,
    val nextMileage: Long?,
    val status: String
)

fun Maintenance.toEntity(): MantenimientoEntity = MantenimientoEntity(
    id = id,
    vehicleId = vehicleId,
    type = type.name,
    date = date.time,
    completionDate = completionDate?.time,
    currentMileage = currentMileage,
    description = description,
    responsible = responsible,
    nextDate = nextDate?.time,
    nextMileage = nextMileage,
    status = status.name
)

fun MantenimientoEntity.toMaintenance(): Maintenance = Maintenance(
    id = id,
    vehicleId = vehicleId,
    type = runCatching { MaintenanceType.valueOf(type) }.getOrDefault(MaintenanceType.GENERAL_REPAIR),
    date = Date(date),
    completionDate = completionDate?.let { Date(it) },
    currentMileage = currentMileage,
    description = description,
    responsible = responsible,
    nextDate = nextDate?.let { Date(it) },
    nextMileage = nextMileage,
    status = runCatching { MaintenanceStatus.valueOf(status) }.getOrDefault(MaintenanceStatus.PENDING)
)
