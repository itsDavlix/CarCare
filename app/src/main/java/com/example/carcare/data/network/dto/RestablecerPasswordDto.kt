package com.example.carcare.data.network.dto

/**
 * Cuerpo de PATCH /api/conductores/{id}/password.
 * Restablecimiento por el admin: solo la contraseña nueva (no pide la actual).
 */
data class RestablecerPasswordDto(
    val passwordNueva: String
)
