package com.example.carcare.model

import java.util.Date

/** Tipo de notificación (espejo de TipoNotificacion del backend). */
enum class NotificationType { VEHICULO, CONDUCTOR, MANTENIMIENTO, ASIGNACION, SEGURIDAD, OTRO }

/** Notificación in-app ya mapeada a dominio. */
data class Notificacion(
    override val id: String,
    val type: NotificationType,
    val message: String,
    val read: Boolean,
    val timestamp: Date
) : Identifiable
