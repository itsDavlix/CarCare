package com.example.carcare.model

import java.util.Date
import java.util.UUID

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
    val status: DriverStatus = DriverStatus.ACTIVE
) : Identifiable {
    val fullName: String get() = "$firstName $lastName"
}