package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de POST /api/auth/cambiar-password (auto-servicio del conductor).
 * Coincide con CambioPasswordDTO del backend: pide la contraseña actual.
 */
@JsonClass(generateAdapter = true)
data class CambioPasswordDto(
    val cedula: String,
    val passwordActual: String,
    val passwordNueva: String
)
