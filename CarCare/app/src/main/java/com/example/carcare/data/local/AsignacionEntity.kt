package com.example.carcare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.FuelLevel
import java.util.Date

@Entity(tableName = "asignaciones_cache")
data class AsignacionEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val driverId: String,
    val departureDate: Long,
    val plannedReturnDate: Long,
    val initialMileage: Long,
    val returnDate: Long?,
    val finalMileage: Long?,
    val departureObservations: String,
    val returnObservations: String,
    val status: String,
    val fuelLevelInitial: String?,
    val conditionOkInitial: Boolean?,
    val acceptanceDate: Long?,
    val fuelLevelFinal: String?,
    val conditionOkFinal: Boolean?,
    val rejectionReason: String?,
    val overdue: Boolean,
    val daysOverdue: Long
)

fun Assignment.toEntity(): AsignacionEntity = AsignacionEntity(
    id = id,
    vehicleId = vehicleId,
    driverId = driverId,
    departureDate = departureDate.time,
    plannedReturnDate = plannedReturnDate.time,
    initialMileage = initialMileage,
    returnDate = returnDate?.time,
    finalMileage = finalMileage,
    departureObservations = departureObservations,
    returnObservations = returnObservations,
    status = status.name,
    fuelLevelInitial = fuelLevelInitial?.name,
    conditionOkInitial = conditionOkInitial,
    acceptanceDate = acceptanceDate?.time,
    fuelLevelFinal = fuelLevelFinal?.name,
    conditionOkFinal = conditionOkFinal,
    rejectionReason = rejectionReason,
    overdue = overdue,
    daysOverdue = daysOverdue
)

fun AsignacionEntity.toAssignment(): Assignment = Assignment(
    id = id,
    vehicleId = vehicleId,
    driverId = driverId,
    departureDate = Date(departureDate),
    plannedReturnDate = Date(plannedReturnDate),
    initialMileage = initialMileage,
    returnDate = returnDate?.let { Date(it) },
    finalMileage = finalMileage,
    departureObservations = departureObservations,
    returnObservations = returnObservations,
    status = runCatching { AssignmentStatus.valueOf(status) }.getOrDefault(AssignmentStatus.ACTIVE),
    fuelLevelInitial = FuelLevel.fromName(fuelLevelInitial),
    conditionOkInitial = conditionOkInitial,
    acceptanceDate = acceptanceDate?.let { Date(it) },
    fuelLevelFinal = FuelLevel.fromName(fuelLevelFinal),
    conditionOkFinal = conditionOkFinal,
    rejectionReason = rejectionReason,
    overdue = overdue,
    daysOverdue = daysOverdue
)
