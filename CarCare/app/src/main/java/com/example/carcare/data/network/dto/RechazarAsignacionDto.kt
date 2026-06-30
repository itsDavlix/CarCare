package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/** Cuerpo de PATCH /api/asignaciones/{id}/rechazar. */
@JsonClass(generateAdapter = true)
data class RechazarAsignacionDto(
    val motivo: String
)
