package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de PATCH /api/conductores/{id}/password.
 * Restablecimiento por el admin: solo la contraseña nueva (no pide la actual).
 */
@JsonClass(generateAdapter = true)
data class RestablecerPasswordDto(
    val passwordNueva: String
)
