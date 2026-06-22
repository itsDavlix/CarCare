package com.example.carcare.data.repository

import com.example.carcare.data.local.AsignacionDao
import com.example.carcare.data.local.toAssignment
import com.example.carcare.data.local.toEntity
import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.AceptarAsignacionDto
import com.example.carcare.data.network.dto.AsignacionRequestDto
import com.example.carcare.data.network.dto.AsignacionResponseDto
import com.example.carcare.data.network.dto.CompletarAsignacionDto
import com.example.carcare.data.network.dto.RechazarAsignacionDto
import com.example.carcare.data.network.formatApiDate
import com.example.carcare.data.network.parseApiDate
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.FuelLevel
import com.example.carcare.model.VehicleStatus
import java.util.Date

/**
 * Conecta el modelo Assignment con la API.
 * El backend coordina los efectos sobre el vehiculo (IN_USE al crear,
 * km+estado al completar, liberar al borrar). El cliente NO los replica.
 */
class AssignmentRepository @javax.inject.Inject constructor(
    private val dao: AsignacionDao
) : CrudRepository<Assignment> {

    private val api = ApiClient.asignacionApi

    override suspend fun getAll(): List<Assignment> = try {
        val remote = api.listar().map { it.toDomain() }
        dao.replaceAll(remote.map { it.toEntity() })
        remote
    } catch (e: Exception) {
        val cached = dao.getAll().map { it.toAssignment() }
        if (cached.isNotEmpty()) cached else throw e
    }

    override suspend fun create(assignment: Assignment): Assignment =
        api.crear(assignment.toRequestDto()).toDomain().also { dao.upsert(it.toEntity()) }

    suspend fun update(assignment: Assignment): Assignment =
        api.actualizar(assignment.id.toLong(), assignment.toRequestDto()).toDomain().also { dao.upsert(it.toEntity()) }

    /** Check-out del conductor: acepta la asignación pendiente (con foto opcional en base64). */
    suspend fun accept(
        id: String,
        initialMileage: Long,
        fuelLevel: FuelLevel,
        conditionOk: Boolean,
        observations: String,
        photoBase64: String?
    ): Assignment {
        val dto = AceptarAsignacionDto(
            kilometrajeInicial = initialMileage,
            nivelCombustible = fuelLevel.name,
            condicionOptima = conditionOk,
            observaciones = observations.ifBlank { null },
            fotoCombustible = photoBase64
        )
        return api.aceptar(id.toLong(), dto).toDomain().also { dao.upsert(it.toEntity()) }
    }

    /** Baja la foto del combustible (base64) por id; null si no hay. */
    suspend fun fetchInitialPhoto(id: String): String? = api.fotoInicial(id.toLong()).foto
    suspend fun fetchFinalPhoto(id: String): String? = api.fotoFinal(id.toLong()).foto

    /** El conductor rechaza la asignación pendiente con un motivo. */
    suspend fun reject(id: String, reason: String): Assignment =
        api.rechazar(id.toLong(), RechazarAsignacionDto(motivo = reason)).toDomain().also { dao.upsert(it.toEntity()) }

    /** Admin: registra el retorno y elige el estado siguiente del vehículo. */
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
        return api.completar(id.toLong(), dto).toDomain().also { dao.upsert(it.toEntity()) }
    }

    /**
     * Check-in del conductor: entrega el vehículo con km, combustible y condición.
     * El backend decide el estado del vehículo (AVAILABLE, o PENDING_REVIEW si no está óptimo),
     * así que no se envía siguienteEstadoVehiculo.
     */
    suspend fun deliver(
        id: String,
        returnDate: Date,
        finalMileage: Long,
        observations: String,
        fuelLevel: FuelLevel,
        conditionOk: Boolean,
        photoBase64: String?
    ): Assignment {
        val dto = CompletarAsignacionDto(
            fechaRetorno = formatApiDate(returnDate),
            kilometrajeFinal = finalMileage,
            observacionesRetorno = observations.ifBlank { null },
            nivelCombustibleFinal = fuelLevel.name,
            condicionOptimaFinal = conditionOk,
            fotoCombustibleFinal = photoBase64
        )
        return api.completar(id.toLong(), dto).toDomain().also { dao.upsert(it.toEntity()) }
    }

    override suspend fun delete(id: String) {
        api.eliminar(id.toLong())
        dao.deleteById(id)
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
    status = runCatching { AssignmentStatus.valueOf(estado) }.getOrDefault(AssignmentStatus.ACTIVE),
    fuelLevelInitial = FuelLevel.fromName(nivelCombustibleInicial),
    conditionOkInitial = condicionOptimaInicial,
    acceptanceDate = parseApiDate(fechaAceptacion),
    fuelLevelFinal = FuelLevel.fromName(nivelCombustibleFinal),
    conditionOkFinal = condicionOptimaFinal,
    rejectionReason = motivoRechazo,
    hasPhotoInitial = tieneFotoInicial,
    hasPhotoFinal = tieneFotoFinal,
    overdue = vencida,
    daysOverdue = diasAtraso
)

private fun Assignment.toRequestDto(): AsignacionRequestDto = AsignacionRequestDto(
    vehiculoId = vehicleId.toLong(),
    conductorId = driverId.toLong(),
    fechaSalida = formatApiDate(departureDate),
    fechaRetornoPlanificada = formatApiDate(plannedReturnDate),
    kilometrajeInicial = initialMileage,
    observacionesSalida = departureObservations.ifBlank { null }
)