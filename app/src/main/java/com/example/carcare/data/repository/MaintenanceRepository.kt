package com.example.carcare.data.repository

import com.example.carcare.data.local.MantenimientoDao
import com.example.carcare.data.local.toEntity
import com.example.carcare.data.local.toMaintenance
import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.CambioEstadoDto
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
class MaintenanceRepository @javax.inject.Inject constructor(
    private val dao: MantenimientoDao
) : CrudRepository<Maintenance> {

    private val api = ApiClient.mantenimientoApi

    override suspend fun getAll(): List<Maintenance> = try {
        val remote = api.listar().map { it.toDomain() }
        dao.replaceAll(remote.map { it.toEntity() })
        remote
    } catch (e: Exception) {
        val cached = dao.getAll().map { it.toMaintenance() }
        if (cached.isNotEmpty()) cached else throw e
    }

    override suspend fun create(maintenance: Maintenance): Maintenance =
        api.crear(maintenance.toRequestDto()).toDomain().also { dao.upsert(it.toEntity()) }

    /**
     * Reporte del conductor: crea el mantenimiento y, en la MISMA operación, el backend
     * pone el vehículo EN REVISIÓN y actualiza su km. Reemplaza las 3 llamadas sueltas
     * (alta + km + estado), una de las cuales (km) era ADMIN-only → daba 403 al conductor.
     */
    suspend fun reportar(maintenance: Maintenance): Maintenance =
        api.crear(maintenance.toRequestDto(enRevision = true)).toDomain().also { dao.upsert(it.toEntity()) }

    suspend fun update(maintenance: Maintenance): Maintenance =
        api.actualizar(maintenance.id.toLong(), maintenance.toRequestDto()).toDomain().also { dao.upsert(it.toEntity()) }

    override suspend fun delete(id: String) {
        api.eliminar(id.toLong())
        dao.deleteById(id)
    }

    suspend fun changeStatus(id: String, status: MaintenanceStatus): Maintenance =
        api.cambiarEstado(id.toLong(), CambioEstadoDto(status.name)).toDomain().also { dao.upsert(it.toEntity()) }
}

// ---------- Mappers DTO <-> dominio ----------

private fun MantenimientoResponseDto.toDomain(): Maintenance = Maintenance(
    id = id.toString(),
    // vehicleId vacío = vehículo eliminado; la UI lo muestra como "Vehículo eliminado"
    vehicleId = vehiculoId?.toString() ?: "",
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

private fun Maintenance.toRequestDto(enRevision: Boolean = false): MantenimientoRequestDto = MantenimientoRequestDto(
    vehiculoId = vehicleId.toLong(),
    tipo = type.name,
    fecha = formatApiDate(date),
    fechaCompletado = formatApiDate(completionDate),
    kilometrajeActual = currentMileage,
    descripcion = description,
    responsable = responsible,
    fechaProxima = formatApiDate(nextDate),
    kilometrajeProximo = nextMileage,
    estado = status.name,
    ponerVehiculoEnRevision = if (enRevision) true else null
)