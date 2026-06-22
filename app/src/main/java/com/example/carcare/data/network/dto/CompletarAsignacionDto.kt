package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de PATCH /api/asignaciones/{id}/completar.
 * siguienteEstadoVehiculo: estado al que queda el vehiculo tras el retorno (ej. "AVAILABLE").
 */
@JsonClass(generateAdapter = true)
data class CompletarAsignacionDto(
    val fechaRetorno: String?,
    val kilometrajeFinal: Long,
    val observacionesRetorno: String? = null,
    val nivelCombustibleFinal: String? = null,
    val condicionOptimaFinal: Boolean? = null,
    val fotoCombustibleFinal: String? = null,
    val siguienteEstadoVehiculo: String? = null
)