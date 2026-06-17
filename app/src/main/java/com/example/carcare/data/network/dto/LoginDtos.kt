package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/**
 * Cuerpo de POST /api/auth/login. La cédula puede ir con o sin guiones:
 * el backend la normaliza igual.
 */
@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    val cedula: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    val token: String,
    val rol: String,             // "ADMIN" | "CONDUCTOR"
    val nombre: String? = null,
    val cedula: String,          // normalizada (13 dígitos + letra)
    val conductorId: Long? = null
)