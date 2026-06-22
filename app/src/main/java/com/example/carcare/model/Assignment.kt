package com.example.carcare.model

import java.util.Date
import java.util.UUID
import androidx.compose.runtime.Immutable

@Immutable
data class Assignment(
    override val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val driverId: String,
    val departureDate: Date = Date(),
    val plannedReturnDate: Date,
    val initialMileage: Long,
    val returnDate: Date? = null,
    val finalMileage: Long? = null,
    val departureObservations: String = "",
    val returnObservations: String = "",
    val status: AssignmentStatus = AssignmentStatus.ACTIVE,
    // Check-out (aceptación) y check-in (entrega)
    val fuelLevelInitial: FuelLevel? = null,
    val conditionOkInitial: Boolean? = null,
    val acceptanceDate: Date? = null,
    val fuelLevelFinal: FuelLevel? = null,
    val conditionOkFinal: Boolean? = null,
    val rejectionReason: String? = null,
    val hasPhotoInitial: Boolean = false,
    val hasPhotoFinal: Boolean = false,
    // Derivados del backend: atraso de devolución
    val overdue: Boolean = false,
    val daysOverdue: Long = 0
) : Identifiable