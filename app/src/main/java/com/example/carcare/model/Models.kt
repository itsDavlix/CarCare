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
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val fuelType: String,
    val mileage: Long,
    val vehiclePhotoUri: String? = null,
    val registrationPhotoUri: String? = null,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val description: String = ""
)

data class User(
    val id: String,
    val name: String,
    val role: Role
)

enum class DriverStatus(val label: String) {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido")
}

data class Driver(
    val id: String = java.util.UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val idCardNumber: String, // Cédula
    val age: Int,
    val phone: String,
    val licenseNumber: String,
    val licenseExpiryDate: Date,
    val profilePhotoUri: String? = null,
    val licensePhotoUri: String? = null,
    val status: DriverStatus = DriverStatus.ACTIVE
) {
    val fullName: String get() = "$firstName $lastName"
}

enum class MaintenanceStatus(val label: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("En proceso"),
    COMPLETED("Completado")
}

enum class MaintenanceType(val label: String) {
    PREVENTIVE("Preventivo"),
    CORRECTIVE("Correctivo"),
    OIL_CHANGE("Cambio de aceite"),
    BRAKES("Revisión de frenos"),
    ENGINE("Revisión de motor"),
    TIRES("Cambio de llantas"),
    BATTERY("Cambio de batería"),
    ALIGNMENT("Alineación y balanceo"),
    GENERAL_REPAIR("Reparación general")
}

data class Maintenance(
    val id: String = java.util.UUID.randomUUID().toString(),
    val vehicleId: String,
    val type: MaintenanceType,
    val date: Date,
    val currentMileage: Long,
    val description: String,
    val responsible: String,
    val nextDate: Date?,
    val nextMileage: Long?,
    val status: MaintenanceStatus = MaintenanceStatus.PENDING
)

enum class AssignmentStatus {
    ACTIVE, COMPLETED
}

data class Assignment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val vehicleId: String,
    val driverId: String,
    val departureDate: Date = Date(),
    val initialMileage: Long,
    val returnDate: Date? = null,
    val finalMileage: Long? = null,
    val departureObservations: String = "",
    val returnObservations: String = "",
    val status: AssignmentStatus = AssignmentStatus.ACTIVE
)
