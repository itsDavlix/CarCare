package com.example.carcare.model

import java.util.Date

enum class Role {
    ADMIN, DRIVER
}

data class User(
    val id: String,
    val name: String,
    val role: Role
)

data class Vehicle(
    val id: String,
    val plate: String,
    val model: String,
    val year: Int,
    val status: String // Available, Maintenance, In Use
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
