package com.example.carcare.data.repository

import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.dto.NotificacionResponseDto
import com.example.carcare.model.Notificacion
import com.example.carcare.model.NotificationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Conecta el modelo Notificacion con la API de notificaciones. */
class NotificacionRepository @javax.inject.Inject constructor() {

    private val api = ApiClient.notificacionApi

    suspend fun listAdmin(): List<Notificacion> = api.listar().map { it.toDomain() }

    suspend fun listConductor(conductorId: Long): List<Notificacion> =
        api.listarConductor(conductorId).map { it.toDomain() }

    suspend fun markRead(id: String) {
        api.marcarLeida(id.toLong())
    }

    /** conductorId null = feed del admin. */
    suspend fun markAll(conductorId: Long?) {
        api.leerTodas(conductorId)
    }
}

// ---------- Mapper DTO -> dominio ----------

private fun NotificacionResponseDto.toDomain(): Notificacion = Notificacion(
    id = id.toString(),
    type = runCatching { NotificationType.valueOf(tipo) }.getOrDefault(NotificationType.OTRO),
    message = mensaje,
    read = leida,
    timestamp = fechaCreacion?.let { parseIso(it) } ?: Date(),
    entityId = entidadId?.toString()
)

/**
 * El backend manda LocalDateTime ISO en UTC ("2026-06-16T15:30:00..."). Se parsea como UTC
 * para obtener el instante correcto; el display (SimpleDateFormat con la zona local) lo
 * muestra en la hora local del dispositivo.
 */
private fun parseIso(value: String): Date? =
    runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(value.take(19))
    }.getOrNull()
