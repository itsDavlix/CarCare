package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/** Respuesta de /api/notificaciones (coincide con NotificacionResponseDTO del backend). */
@JsonClass(generateAdapter = true)
data class NotificacionResponseDto(
    val id: Long,
    val tipo: String,
    val mensaje: String,
    val audiencia: String,
    val conductorId: Long? = null,
    val entidadId: Long? = null,
    val leida: Boolean = false,
    val fechaCreacion: String? = null
)
