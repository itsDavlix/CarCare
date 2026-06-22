package com.example.carcare.data.repository

import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.CambioEstadoDto
import com.example.carcare.data.network.dto.ConductorRequestDto
import com.example.carcare.data.network.dto.ConductorResponseDto
import com.example.carcare.data.network.dto.RestablecerPasswordDto
import com.example.carcare.data.network.formatApiDate
import com.example.carcare.data.network.parseApiDate
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus
import java.util.Date

/**
 * Conecta el modelo Driver con la API de conductores.
 * Convierte id Long<->String y fecha Date<->String en el borde de red.
 */
class DriverRepository @javax.inject.Inject constructor() : CrudRepository<Driver> {

    private val api = ApiClient.conductorApi

    override suspend fun getAll(): List<Driver> = api.listar().map { it.toDomain() }

    override suspend fun create(driver: Driver): Driver =
        api.crear(driver.toRequestDto()).toDomain()

    suspend fun update(driver: Driver): Driver =
        api.actualizar(driver.id.toLong(), driver.toRequestDto()).toDomain()

    override suspend fun delete(id: String) {
        api.eliminar(id.toLong())
    }

    suspend fun changeStatus(id: String, status: DriverStatus): Driver =
        api.cambiarEstado(id.toLong(), CambioEstadoDto(status.name)).toDomain()

    /** Restablece la contraseña de acceso del conductor (acción de admin). */
    suspend fun resetPassword(id: String, newPassword: String) {
        api.restablecerPassword(id.toLong(), RestablecerPasswordDto(newPassword))
    }
}

// ---------- Mappers DTO <-> dominio ----------

private fun ConductorResponseDto.toDomain(): Driver = Driver(
    id = id.toString(),
    firstName = nombre,
    lastName = apellido,
    idCardNumber = cedula,
    age = edad,
    phone = telefono,
    licenseNumber = numeroLicencia,
    // El backend exige fechaVencimientoLicencia, asi que en la practica nunca es null.
    licenseExpiryDate = parseApiDate(fechaVencimientoLicencia) ?: Date(),
    profilePhotoUri = fotoPerfilUri,
    licensePhotoUri = fotoLicenciaUri,
    status = runCatching { DriverStatus.valueOf(estado) }.getOrDefault(DriverStatus.ACTIVE),
    isAdmin = esAdmin
)

private fun Driver.toRequestDto(): ConductorRequestDto = ConductorRequestDto(
    nombre = firstName,
    apellido = lastName,
    cedula = idCardNumber,
    edad = age,
    telefono = phone,
    numeroLicencia = licenseNumber,
    fechaVencimientoLicencia = formatApiDate(licenseExpiryDate),
    fotoPerfilUri = profilePhotoUri,
    fotoLicenciaUri = licensePhotoUri,
    estado = status.name
)