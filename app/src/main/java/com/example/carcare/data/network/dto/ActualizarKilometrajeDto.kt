package com.example.carcare.data.network.dto

/**
 * Cuerpo de PATCH /api/vehiculos/{id}/kilometraje. Backend espera { "kilometraje": <Long> }.
 */
data class ActualizarKilometrajeDto(
    val kilometraje: Long
)