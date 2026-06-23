package com.example.carcare.model

import java.util.Date
import java.util.UUID
import androidx.compose.runtime.Immutable

@Immutable
data class Vehicle(
    override val id: String = UUID.randomUUID().toString(),
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
) : Identifiable {
    /** El seguro está vencido si la fecha actual superó la fecha de vencimiento. */
    val isInsuranceExpired: Boolean get() = insuranceExpiryDate?.before(java.util.Date()) ?: false

    /** El seguro está por vencer si falta menos de una semana (7 días). */
    val isInsuranceExpiringSoon: Boolean get() {
        val expiry = insuranceExpiryDate ?: return false
        val now = java.util.Date()
        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
        val limit = java.util.Date(now.time + sevenDaysMillis)
        return !isInsuranceExpired && expiry.before(limit)
    }

    /**
     * Estado efectivo para visualización y asignación: si el seguro venció,
     * el vehículo se considera FUERA DE SERVICIO aunque su estado nominal sea DISPONIBLE.
     */
    val effectiveStatus: VehicleStatus get() = when {
        isInsuranceExpired -> VehicleStatus.OUT_OF_SERVICE
        else -> status
    }
}