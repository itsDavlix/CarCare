package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de PATCH /api/asignaciones/{id}/aceptar (check-out del conductor).
 * nivelCombustible es el nombre del enum NivelCombustible (ej. "MEDIO").
 */
@JsonClass(generateAdapter = true)
data class AceptarAsignacionDto(
    val kilometrajeInicial: Long,
    val nivelCombustible: String,
    val condicionOptima: Boolean,
    val observaciones: String? = null,
    val fotoCombustible: String? = null
)
