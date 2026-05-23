package com.example.carcare.model

import java.util.Date

enum class Role {
    ADMIN, DRIVER
}

enum class VehicleStatus(val label: String) {
    AVAILABLE("Disponible"),
    IN_USE("En uso"),
    MAINTENANCE("En mantenimiento"),
    OUT_OF_SERVICE("Fuera de servicio")
}

data class Vehicle(
    val id: String = java.util.UUID.randomUUID().toString(),
    val unitCode: String,
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val fuelType: String,
    val mileage: Long,
    val vehicleType: String,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val description: String = ""
)

data class User(
    val id: String,
    val name: String,
    val role: Role
)

data class Maintenance(
    val id: String,
    val vehicleId: String,
    val date: Date,
    val description: String,
    val cost: Double
)

data class Assignment(
    val id: String,
    val vehicleId: String,
    val driverId: String,
    val date: Date
)
