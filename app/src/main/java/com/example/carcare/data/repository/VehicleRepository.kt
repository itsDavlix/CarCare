package com.example.carcare.data.repository

import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.ActualizarKilometrajeDto
import com.example.carcare.data.network.dto.CambioEstadoDto
import com.example.carcare.data.network.dto.VehiculoRequestDto
import com.example.carcare.data.network.dto.VehiculoResponseDto
import com.example.carcare.data.network.formatApiDate
import com.example.carcare.data.network.parseApiDate
import com.example.carcare.model.FuelType
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus

/**
 * Conecta el modelo de dominio Vehicle con la API.
 * Convierte id Long<->String y fechas Date<->String en el borde de red.
 */
class VehicleRepository : CrudRepository<Vehicle> {

    private val api = ApiClient.vehiculoApi

    override suspend fun getAll(): List<Vehicle> = api.listar().map { it.toDomain() }

    override suspend fun create(vehicle: Vehicle): Vehicle =
        api.crear(vehicle.toRequestDto()).toDomain()

    suspend fun update(vehicle: Vehicle): Vehicle =
        api.actualizar(vehicle.id.toLong(), vehicle.toRequestDto()).toDomain()

    override suspend fun delete(id: String) {
        api.eliminar(id.toLong())
    }

    suspend fun changeStatus(id: String, status: VehicleStatus): Vehicle =
        api.cambiarEstado(id.toLong(), CambioEstadoDto(status.name)).toDomain()

    suspend fun updateMileage(id: String, mileage: Long): Vehicle =
        api.actualizarKilometraje(id.toLong(), ActualizarKilometrajeDto(mileage)).toDomain()
}

// ---------- Mappers DTO <-> dominio ----------

private fun VehiculoResponseDto.toDomain(): Vehicle = Vehicle(
    id = id.toString(),
    brand = marca,
    model = modelo,
    year = anio,
    plate = placa,
    fuelType = parseFuelType(combustible),
    mileage = kilometraje,
    color = color ?: "",
    chassisNumber = numeroChasis ?: "",
    engineNumber = numeroMotor ?: "",
    insurancePolicy = polizaSeguro ?: "",
    insuranceExpiryDate = parseApiDate(fechaVencimientoSeguro),
    circulationExpiryDate = parseApiDate(fechaVencimientoCirculacion),
    vehiclePhotoUri = fotoUri,
    registrationPhotoUri = fotoCirculacionUri,
    insurancePhotoUri = fotoSeguroUri,
    status = runCatching { VehicleStatus.valueOf(estado) }.getOrDefault(VehicleStatus.AVAILABLE),
    description = descripcion ?: ""
)

private fun Vehicle.toRequestDto(): VehiculoRequestDto = VehiculoRequestDto(
    marca = brand,
    modelo = model,
    anio = year,
    placa = plate,
    combustible = fuelType.name,
    kilometraje = mileage,
    color = color.ifBlank { null },
    numeroChasis = chassisNumber.ifBlank { null },
    numeroMotor = engineNumber.ifBlank { null },
    polizaSeguro = insurancePolicy.ifBlank { null },
    fechaVencimientoSeguro = formatApiDate(insuranceExpiryDate),
    fechaVencimientoCirculacion = formatApiDate(circulationExpiryDate),
    fotoUri = vehiclePhotoUri,
    fotoCirculacionUri = registrationPhotoUri,
    fotoSeguroUri = insurancePhotoUri,
    estado = status.name,
    descripcion = description.ifBlank { null }
)

/**
 * Backend manda el nombre del enum (ej: "DIESEL", "GASOLINE", "HEV", "ELECTRIC").
 * Tolera valores legados de la BD ("HYBRID", etiquetas en español) mapeándolos
 * a la constante interna; si llega algo inesperado, default a GASOLINE.
 */
private fun parseFuelType(name: String): FuelType = when (name.trim().uppercase()) {
    "HEV", "HYBRID", "HIBRIDO", "HÍBRIDO" -> FuelType.HEV
    "DIESEL", "DIÉSEL" -> FuelType.DIESEL
    "ELECTRIC", "ELECTRICO", "ELÉCTRICO" -> FuelType.ELECTRIC
    else -> FuelType.entries.firstOrNull {
        it.name == name.trim().uppercase() || it.label.equals(name.trim(), true)
    } ?: FuelType.GASOLINE
}