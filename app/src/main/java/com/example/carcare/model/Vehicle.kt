package com.example.carcare.model

import java.util.Date
import java.util.UUID

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
) : Identifiable