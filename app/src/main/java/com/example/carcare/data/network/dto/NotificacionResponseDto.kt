package com.example.carcare.data.network.dto

/** Respuesta de /api/notificaciones (coincide con NotificacionResponseDTO del backend). */
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
