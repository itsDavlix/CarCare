package com.example.carcare.data.network.dto

/**
 * Cuerpo de PATCH /api/asignaciones/{id}/completar.
 * siguienteEstadoVehiculo: estado al que queda el vehiculo tras el retorno (ej. "AVAILABLE").
 */
data class CompletarAsignacionDto(
    val fechaRetorno: String?,
    val kilometrajeFinal: Long,
    val observacionesRetorno: String? = null,
    val siguienteEstadoVehiculo: String? = null
)