package com.example.carcare.model

import java.util.Date
import java.util.UUID
import androidx.compose.runtime.Immutable

@Immutable
data class Driver(
    override val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val idCardNumber: String,
    val age: Int,
    val phone: String,
    val licenseNumber: String,
    val licenseExpiryDate: Date,
    val profilePhotoUri: String? = null,
    val licensePhotoUri: String? = null,
    val status: DriverStatus = DriverStatus.ACTIVE,
    /** La cédula pertenece a una cuenta ADMIN: no es asignable a un vehículo. */
    val isAdmin: Boolean = false
) : Identifiable {
    val fullName: String get() = "$firstName $lastName"

    /** La licencia está vencida si la fecha actual superó la fecha de vencimiento. */
    val isLicenseExpired: Boolean get() = licenseExpiryDate.before(java.util.Date())

    /** La licencia está por vencer si falta menos de una semana (7 días). */
    val isLicenseExpiringSoon: Boolean get() {
        val now = java.util.Date()
        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
        val limit = java.util.Date(now.time + sevenDaysMillis)
        return !isLicenseExpired && licenseExpiryDate.before(limit)
    }

    /** 
     * Estado efectivo para visualización: si la licencia venció, se considera INACTIVO 
     * aunque su estado nominal sea ACTIVO.
     */
    val effectiveStatus: DriverStatus get() = if (isLicenseExpired) DriverStatus.INACTIVE else status
}