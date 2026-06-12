package com.example.carcare.data.repository

import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.AsignacionRequestDto
import com.example.carcare.data.network.dto.AsignacionResponseDto
import com.example.carcare.data.network.dto.CompletarAsignacionDto
import com.example.carcare.data.network.formatApiDate
import com.example.carcare.data.network.parseApiDate
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.VehicleStatus
import java.util.Date

/**
 * Conecta el modelo Assignment con la API.
 * El backend coordina los efectos sobre el vehiculo (IN_USE al crear,
 * km+estado al completar, liberar al borrar). El cliente NO los replica.
 */
class AssignmentRepository : CrudRepository<Assignment> {

    private val api = ApiClient.asignacionApi

    override suspend fun getAll(): List<Assignment> = api.listar().map { it.toDomain() }

    override suspend fun create(assignment: Assignment): Assignment =
        api.crear(assignment.toRequestDto()).toDomain()

    suspend fun update(assignment: Assignment): Assignment =
        api.actualizar(assignment.id.toLong(), assignment.toRequestDto()).toDomain()

    suspend fun complete(
        id: String,
        returnDate: Date,
        finalMileage: Long,
        observations: String,
        nextStatus: VehicleStatus
    ): Assignment {
        val dto = CompletarAsignacionDto(
            fechaRetorno = formatApiDate(returnDate),
            kilometrajeFinal = finalMileage,
            observacionesRetorno = observations.ifBlank { null },
            siguienteEstadoVehiculo = nextStatus.name
        )
        return api.completar(id.toLong(), dto).toDomain()
    }

    override suspend fun delete(id: String) {
        api.eliminar(id.toLong())
    }
}

// ---------- Mappers DTO <-> dominio ----------

private fun AsignacionResponseDto.toDomain(): Assignment = Assignment(
    id = id.toString(),
    // vehicleId vacío = vehículo eliminado; la UI lo muestra como "Vehículo eliminado"
    vehicleId = vehiculoId?.toString() ?: "",
    driverId = conductorId.toString(),
    departureDate = parseApiDate(fechaSalida) ?: Date(),
    plannedReturnDate = parseApiDate(fechaRetornoPlanificada) ?: Date(),
    initialMileage = kilometrajeInicial,
    returnDate = parseApiDate(fechaRetorno),
    finalMileage = kilometrajeFinal,
    departureObservations = observacionesSalida ?: "",
    returnObservations = observacionesRetorno ?: "",
    status = runCatching { AssignmentStatus.valueOf(estado) }.getOrDefault(AssignmentStatus.ACTIVE)
)

private fun Assignment.toRequestDto(): AsignacionRequestDto = AsignacionRequestDto(
    vehiculoId = vehicleId.toLong(),
    conductorId = driverId.toLong(),
    fechaSalida = formatApiDate(departureDate),
    fechaRetornoPlanificada = formatApiDate(plannedReturnDate),
    kilometrajeInicial = initialMileage,
    observacionesSalida = departureObservations.ifBlank { null }
)