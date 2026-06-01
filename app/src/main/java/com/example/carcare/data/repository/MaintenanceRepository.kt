package com.example.carcare.data.repository

import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.MantenimientoRequestDto
import com.example.carcare.data.network.dto.MantenimientoResponseDto
import com.example.carcare.data.network.formatApiDate
import com.example.carcare.data.network.parseApiDate
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.MaintenanceType
import java.util.Date

/**
 * Conecta el modelo Maintenance con la API de mantenimientos.
 * Convierte id y vehicleId Long<->String y fechas Date<->String.
 */
class MaintenanceRepository : CrudRepository<Maintenance> {

    private val api = ApiClient.mantenimientoApi

    override suspend fun getAll(): List<Maintenance> = api.listar().map { it.toDomain() }

    override suspend fun create(maintenance: Maintenance): Maintenance =
        api.crear(maintenance.toRequestDto()).toDomain()

    suspend fun update(maintenance: Maintenance): Maintenance =
        api.actualizar(maintenance.id.toLong(), maintenance.toRequestDto()).toDomain()

    override suspend fun delete(id: String) {
        api.eliminar(id.toLong())
    }

    suspend fun changeStatus(id: String, status: MaintenanceStatus): Maintenance =
        api.cambiarEstado(id.toLong(), mapOf("estado" to status.name)).toDomain()
}

// ---------- Mappers DTO <-> dominio ----------

private fun MantenimientoResponseDto.toDomain(): Maintenance = Maintenance(
    id = id.toString(),
    vehicleId = vehiculoId.toString(),
    type = runCatching { MaintenanceType.valueOf(tipo) }.getOrDefault(MaintenanceType.GENERAL_REPAIR),
    date = parseApiDate(fecha) ?: Date(),
    completionDate = parseApiDate(fechaCompletado),
    currentMileage = kilometrajeActual,
    description = descripcion,
    responsible = responsable,
    nextDate = parseApiDate(fechaProxima),
    nextMileage = kilometrajeProximo,
    status = runCatching { MaintenanceStatus.valueOf(estado) }.getOrDefault(MaintenanceStatus.PENDING)
)

private fun Maintenance.toRequestDto(): MantenimientoRequestDto = MantenimientoRequestDto(
    vehiculoId = vehicleId.toLong(),
    tipo = type.name,
    fecha = formatApiDate(date),
    fechaCompletado = formatApiDate(completionDate),
    kilometrajeActual = currentMileage,
    descripcion = description,
    responsable = responsible,
    fechaProxima = formatApiDate(nextDate),
    kilometrajeProximo = nextMileage,
    estado = status.name
)