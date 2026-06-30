package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de PATCH /api/vehiculos/{id}/kilometraje. Backend espera { "kilometraje": <Long> }.
 */
@JsonClass(generateAdapter = true)
data class ActualizarKilometrajeDto(
    val kilometraje: Long
)