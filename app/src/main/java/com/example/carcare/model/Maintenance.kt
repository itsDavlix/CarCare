package com.example.carcare.model

import java.util.Date
import java.util.UUID

data class Maintenance(
    override val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val type: MaintenanceType,
    val date: Date,
    val completionDate: Date? = null,
    val currentMileage: Long,
    val description: String,
    val responsible: String,
    val nextDate: Date?,
    val nextMileage: Long?,
    val status: MaintenanceStatus = MaintenanceStatus.IN_PROGRESS
) : Identifiable