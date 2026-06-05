package com.example.carcare.data.network.dto

/**
 * Cuerpo de los PATCH /{id}/estado (vehiculos, conductores, mantenimientos).
 * El backend espera { "estado": "<NOMBRE_DEL_ENUM>" }. Mismo shape para los tres.
 */
data class CambioEstadoDto(
    val estado: String
)