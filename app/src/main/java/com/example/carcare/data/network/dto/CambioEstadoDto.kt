package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de los PATCH /{id}/estado (vehiculos, conductores, mantenimientos).
 * El backend espera { "estado": "<NOMBRE_DEL_ENUM>" }. Mismo shape para los tres.
 */
@JsonClass(generateAdapter = true)
data class CambioEstadoDto(
    val estado: String
)