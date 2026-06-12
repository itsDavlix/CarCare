package com.example.carcare.model

import java.util.Date
import java.util.UUID
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
    val status: AssignmentStatus = AssignmentStatus.ACTIVE
) : Identifiable