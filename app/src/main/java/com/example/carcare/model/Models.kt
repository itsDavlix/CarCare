package com.example.carcare.model

import java.util.Date

enum class Role {
    ADMIN, DRIVER
}

enum class VehicleStatus(val label: String) {
    AVAILABLE("Disponible"),
    ASSIGNED("Asignado"),
    IN_USE("En uso"),
    PENDING_REVIEW("Pendiente de revisión"),
    MAINTENANCE("En mantenimiento"),
    OUT_OF_SERVICE("Fuera de servicio"),
    INACTIVE("Inactivo")
}

/**
 * Tipos de combustible disponibles.
 * Se serializa a String al guardar en Vehicle.fuelType para mantener
 * compatibilidad con datos existentes y futura persistencia.
 */
enum class FuelType(val label: String) {
    GASOLINE("Gasolina"),
    DIESEL("Diésel"),
    HEV("HEV");

    companion object {
        /** Busca un FuelType por su label. Retorna null si no se encuentra. */
        fun fromLabel(label: String): FuelType? =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}

data class Vehicle(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val fuelType: FuelType,
    val mileage: Long,
    val color: String = "",
    val chassisNumber: String = "",
    val engineNumber: String = "",
    val insurancePolicy: String = "",
    val insuranceExpiryDate: Date? = null,
    val circulationExpiryDate: Date? = null,
    val vehiclePhotoUri: String? = null,
    val registrationPhotoUri: String? = null,
    val insurancePhotoUri: String? = null,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val description: String = ""
) : Identifiable

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
    override val id: String = java.util.UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val idCardNumber: String,
    val age: Int,
    val phone: String,
    val licenseNumber: String,
    val licenseExpiryDate: Date,
    val profilePhotoUri: String? = null,
    val licensePhotoUri: String? = null,
    val status: DriverStatus = DriverStatus.ACTIVE
) : Identifiable {
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
    override val id: String = java.util.UUID.randomUUID().toString(),
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

enum class AssignmentStatus {
    ACTIVE, COMPLETED
}

data class Assignment(
    override val id: String = java.util.UUID.randomUUID().toString(),
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