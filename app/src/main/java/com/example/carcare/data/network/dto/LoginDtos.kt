package com.example.carcare.data.network.dto

/**
 * Cuerpo de POST /api/auth/login. La cédula puede ir con o sin guiones:
 * el backend la normaliza igual.
 */
data class LoginRequestDto(
    val cedula: String,
    val password: String
)

data class LoginResponseDto(
    val token: String,
    val rol: String,             // "ADMIN" | "CONDUCTOR"
    val nombre: String? = null,
    val cedula: String,          // normalizada (13 dígitos + letra)
    val conductorId: Long? = null
)