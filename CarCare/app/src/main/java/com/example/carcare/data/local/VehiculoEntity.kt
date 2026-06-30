package com.example.carcare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.carcare.model.FuelType
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import java.util.Date

/**
 * Espejo local de [Vehicle] para el caché Room. Tipos primitivos (fechas como epoch millis,
 * enums como String) para no necesitar TypeConverters; la conversión va en los mappers.
 */
@Entity(tableName = "vehiculos_cache")
data class VehiculoEntity(
    @PrimaryKey val id: String,
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val fuelType: String,
    val mileage: Long,
    val color: String,
    val chassisNumber: String,
    val engineNumber: String,
    val insurancePolicy: String,
    val insuranceExpiryDate: Long?,
    val circulationExpiryDate: Long?,
    val vehiclePhotoUri: String?,
    val registrationPhotoUri: String?,
    val insurancePhotoUri: String?,
    val status: String,
    val description: String
)

fun Vehicle.toEntity(): VehiculoEntity = VehiculoEntity(
    id = id,
    brand = brand,
    model = model,
    year = year,
    plate = plate,
    fuelType = fuelType.name,
    mileage = mileage,
    color = color,
    chassisNumber = chassisNumber,
    engineNumber = engineNumber,
    insurancePolicy = insurancePolicy,
    insuranceExpiryDate = insuranceExpiryDate?.time,
    circulationExpiryDate = circulationExpiryDate?.time,
    vehiclePhotoUri = vehiclePhotoUri,
    registrationPhotoUri = registrationPhotoUri,
    insurancePhotoUri = insurancePhotoUri,
    status = status.name,
    description = description
)

fun VehiculoEntity.toVehicle(): Vehicle = Vehicle(
    id = id,
    brand = brand,
    model = model,
    year = year,
    plate = plate,
    fuelType = runCatching { FuelType.valueOf(fuelType) }.getOrDefault(FuelType.GASOLINE),
    mileage = mileage,
    color = color,
    chassisNumber = chassisNumber,
    engineNumber = engineNumber,
    insurancePolicy = insurancePolicy,
    insuranceExpiryDate = insuranceExpiryDate?.let { Date(it) },
    circulationExpiryDate = circulationExpiryDate?.let { Date(it) },
    vehiclePhotoUri = vehiclePhotoUri,
    registrationPhotoUri = registrationPhotoUri,
    insurancePhotoUri = insurancePhotoUri,
    status = runCatching { VehicleStatus.valueOf(status) }.getOrDefault(VehicleStatus.AVAILABLE),
    description = description
)
