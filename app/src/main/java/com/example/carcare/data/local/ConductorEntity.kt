package com.example.carcare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus
import java.util.Date

@Entity(tableName = "conductores_cache")
data class ConductorEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val idCardNumber: String,
    val age: Int,
    val phone: String,
    val licenseNumber: String,
    val licenseExpiryDate: Long,
    val profilePhotoUri: String?,
    val licensePhotoUri: String?,
    val status: String,
    val isAdmin: Boolean
)

fun Driver.toEntity(): ConductorEntity = ConductorEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    idCardNumber = idCardNumber,
    age = age,
    phone = phone,
    licenseNumber = licenseNumber,
    licenseExpiryDate = licenseExpiryDate.time,
    profilePhotoUri = profilePhotoUri,
    licensePhotoUri = licensePhotoUri,
    status = status.name,
    isAdmin = isAdmin
)

fun ConductorEntity.toDriver(): Driver = Driver(
    id = id,
    firstName = firstName,
    lastName = lastName,
    idCardNumber = idCardNumber,
    age = age,
    phone = phone,
    licenseNumber = licenseNumber,
    licenseExpiryDate = Date(licenseExpiryDate),
    profilePhotoUri = profilePhotoUri,
    licensePhotoUri = licensePhotoUri,
    status = runCatching { DriverStatus.valueOf(status) }.getOrDefault(DriverStatus.ACTIVE),
    isAdmin = isAdmin
)
