package com.example.carcare.data.network.dto

/**
 * Cuerpo de POST /api/auth/cambiar-password (auto-servicio del conductor).
 * Coincide con CambioPasswordDTO del backend: pide la contraseña actual.
 */
data class CambioPasswordDto(
    val cedula: String,
    val passwordActual: String,
    val passwordNueva: String
)
